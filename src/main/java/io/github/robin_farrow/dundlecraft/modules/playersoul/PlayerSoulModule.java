package io.github.robin_farrow.dundlecraft.modules.playersoul;

import com.mojang.serialization.Codec;
import io.github.robin_farrow.dundlecraft.DundleCraft;
import io.github.robin_farrow.dundlecraft.core.DundleModule;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class PlayerSoulModule extends DundleModule
{
    public static final int MAX_SOUL = 3;
    public static final int[] HEARTS_BY_SOUL_LEFT = {10, 10, 8, 6};

    public static final Identifier SOUL_EMPTY = Identifier.fromNamespaceAndPath(DundleCraft.MODID, "hud/soul_empty");
    public static final Identifier SOUL_FULL = Identifier.fromNamespaceAndPath(DundleCraft.MODID, "hud/soul_full");

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, DundleCraft.MODID);

    // Saved with the player, copied through death, and synced to the client for the HUD
    public static final Supplier<AttachmentType<Integer>> SOUL = ATTACHMENTS.register("soul", () ->
            AttachmentType.builder(() -> MAX_SOUL)
                    .serialize(Codec.INT.fieldOf("soul"))
                    .copyOnDeath()
                    .sync(ByteBufCodecs.VAR_INT)
                    .build());

    @Override
    public void register(IEventBus modEventBus)
    {
        ATTACHMENTS.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new SoulTabListDisplay());
    }

    // How much soul an item restores when held in the other hand during a totem resurrection
    public static int soulRestoredBy(ItemStack stack)
    {
        return stack.is(Items.NETHER_STAR) ? 2 : stack.is(Items.DRAGON_EGG) ? 1 : 0;
    }

    private static void setSoul(Player player, int soul) {

        int clamped = Mth.clamp(soul, 0, MAX_SOUL);
        player.setData(SOUL, clamped);

        // ApplyMaxHealth: 3 lives left = 6 hearts, 2 = 8 hearts, 1 = 10 hearts
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(HEARTS_BY_SOUL_LEFT[clamped] * 2.0);
        }
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        int soul = player.getData(SOUL);

        // A player with 0 soul can only be here if an admin unbanned them: give them a fresh set
        if (soul <= 0) {
            soul = MAX_SOUL;
        }
        setSoul(player, soul); // writes it and syncs it to the client
    }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        Player player = event.getEntity();
        player.setHealth(player.getMaxHealth());
    }

    // Runs inside the totem check, before the totem is consumed. The egg or star is in the OTHER hand.
    @SubscribeEvent
    public void onTotem(LivingUseTotemEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        int soul = player.getData(SOUL);
        if (soul >= MAX_SOUL) return;

        InteractionHand otherHand = event.getHandHolding() == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack held = player.getItemInHand(otherHand);

        int restored = soulRestoredBy(held);
        if (restored == 0) return;

        int newSoul = Math.min(MAX_SOUL, soul + restored);
        held.shrink(1);
        setSoul(player, newSoul);

        int gained = newSoul - soul;
        player.sendSystemMessage(Component.literal("Restored " + gained + " Soul").withStyle(ChatFormatting.AQUA));
    }

    // LOWEST so that if another mod cancels the death, we don't take a life for it
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDeath(LivingDeathEvent event)
    {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        int remaining = player.getData(SOUL) - 1;
        setSoul(player, remaining);
        if (remaining > 0) return;

        MinecraftServer server = player.level().getServer();
        NameAndId id = player.nameAndId();
        UserBanList bans = server.getPlayerList().getBans();
        if (!bans.isBanned(id)) {
            bans.add(new UserBanListEntry(id, null, "DundleCraft", null, "You ran out of soul"));
        }
        // Queue the kick so the death (drops, death message) finishes first
        server.execute(() -> player.connection.disconnect(Component.literal("You have run out of soul.")));
    }
}
