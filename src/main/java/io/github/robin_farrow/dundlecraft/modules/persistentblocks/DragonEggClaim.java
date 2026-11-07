package io.github.robin_farrow.dundlecraft.modules.persistentblocks;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class DragonEggClaim
{
    private DragonEggClaim()
    {
    }

    private static final Component ONLY_A_DRAGON =
            Component.literal("Only a Dragon can claim the Egg").withStyle(ChatFormatting.DARK_PURPLE);

    // Returns true when the egg was claimed, so it shouldn't teleport
    public static boolean tryClaim(Level level, BlockPos pos, Player player)
    {
        if (!player.getItemBySlot(EquipmentSlot.HEAD).is(Items.DRAGON_HEAD))
        {
            player.sendOverlayMessage(ONLY_A_DRAGON);
            return false;
        }

        Inventory inventory = player.getInventory();
        if (!InventoryFullWarning.hasEmptySlot(inventory))
        {
            InventoryFullWarning.warnFull(player);
            return false; // still teleports, so the egg can't be mined instead
        }

        // The client only skips its teleport particles; the server does the claiming
        if (player instanceof ServerPlayer serverPlayer)
        {
            inventory.add(new ItemStack(Items.DRAGON_EGG));
            level.removeBlock(pos, false);
            serverPlayer.connection.send(new ClientboundSoundPacket(
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.PARROT_IMITATE_ENDER_DRAGON),
                    SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(),
                    0.5F, 1.0F, level.getRandom().nextLong()));
            InventoryFullWarning.warnIfJustFilled(inventory, true);
        }
        return true;
    }
}
