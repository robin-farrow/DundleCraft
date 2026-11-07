package io.github.robin_farrow.dundlecraft.modules.playersoul;

import io.github.robin_farrow.dundlecraft.DundleCraft;
import net.minecraft.ChatFormatting;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.objects.AtlasSprite;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SoulTabListDisplay
{
    private static final String LIST_OBJECTIVE = "soul";
    private static final int MAX_HEARTS = 10;
    private static final Identifier HEART_FULL = Identifier.fromNamespaceAndPath(DundleCraft.MODID, "hud/hardcore_full_with_container");
    private static final Identifier HEART_HALF = Identifier.fromNamespaceAndPath(DundleCraft.MODID, "hud/hardcore_half_with_container");
    private static final Identifier HEART_EMPTY = Identifier.fromNamespaceAndPath(DundleCraft.MODID, "hud/container_hardcore");
    private static final Identifier SKULL = Identifier.fromNamespaceAndPath(DundleCraft.MODID, "hud/skull");

    private static final FontDescription PAD_FONT =
            new FontDescription.Resource(Identifier.fromNamespaceAndPath(DundleCraft.MODID, "pad"));


    private final Map<UUID, FixedFormat> lastSent = new HashMap<>();

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event)
    {
        ServerScoreboard scoreboard = event.getServer().getScoreboard();
        Objective objective = scoreboard.getObjective(LIST_OBJECTIVE);
        if (objective == null) {
            objective = scoreboard.addObjective(LIST_OBJECTIVE, ObjectiveCriteria.DUMMY,
                    Component.literal("Soul"), ObjectiveCriteria.RenderType.INTEGER, false, null);
        }
        scoreboard.setDisplayObjective(DisplaySlot.LIST, objective);
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event)
    {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 10 != 0) return;

        ServerScoreboard scoreboard = server.getScoreboard();
        Objective objective = scoreboard.getObjective(LIST_OBJECTIVE);
        if (objective == null) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            FixedFormat format = new FixedFormat(buildColumn(player));
            if (!format.equals(lastSent.get(player.getUUID()))) {          // only send when it changed
                lastSent.put(player.getUUID(), format);
                scoreboard.getOrCreatePlayerScore(player, objective).numberFormatOverride(format);
            }
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        lastSent.remove(event.getEntity().getUUID());
    }

    private static MutableComponent sprite(Identifier id) {
        return Component.object(new AtlasSprite(AtlasIds.GUI, id));
    }

    private static MutableComponent buildColumn(ServerPlayer player)
    {
        MutableComponent column = Component.empty();

        // Souls: always MAX_SOULS sprites, 8px each
        int soul = player.getData(PlayerSoulModule.SOUL);
        for (int i = 0; i < PlayerSoulModule.MAX_SOUL; i++) {
            column.append(sprite(i < soul ? PlayerSoulModule.SOUL_FULL : PlayerSoulModule.SOUL_EMPTY));
        }
        column.append("  ");                                               // 8px gap

        // Hearts: the ones they have, then blank space for the ones they don't (2 spaces = 8px)
        int hearts = Mth.ceil(player.getMaxHealth() / 2.0F);
        int halves = Mth.ceil(player.getHealth());
        for (int i = 0; i < hearts; i++) {
            column.append(sprite(halves >= i * 2 + 2 ? HEART_FULL
                    : halves == i * 2 + 1 ? HEART_HALF
                    : HEART_EMPTY));
        }
        column.append(" ".repeat((MAX_HEARTS - hearts) * 2 + 2)); // missing hearts + 8px gap

        // Deaths: left-aligned in a fixed 2-digit box (12px).
        // Each missing digit becomes a 6px spacer after the number.
        String deaths = String.valueOf(Math.min(99, player.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS))));
        column.append(sprite(SKULL));
        column.append(Component.literal("\ue002").withStyle(style -> style.withFont(PAD_FONT)));  // 2px gap
        column.append(Component.literal(deaths).withStyle(ChatFormatting.DARK_RED));
        for (int i = deaths.length(); i < 2; i++) {
            column.append(Component.literal("\ue006").withStyle(style -> style.withFont(PAD_FONT)));
        }
        column.append("  ");
        return column;
    }
}
