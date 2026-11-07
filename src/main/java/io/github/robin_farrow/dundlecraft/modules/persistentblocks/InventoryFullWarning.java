package io.github.robin_farrow.dundlecraft.modules.persistentblocks;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public final class InventoryFullWarning
{
    private InventoryFullWarning() {}

    private static final Component NOW_FULL =
            Component.literal("Inventory is now full").withStyle(ChatFormatting.WHITE);

    private static final Component FULL =
            Component.literal("Inventory is full").withStyle(ChatFormatting.WHITE);

    public static void warnFull(Player player) {
        player.sendOverlayMessage(FULL);
    }

    public static boolean hasEmptySlot(Inventory inventory) {
        return inventory.getFreeSlot() >= 0;
    }

    // Warns only when the inventory had an empty slot before and now has none
    public static void warnIfJustFilled(Inventory inventory, boolean hadEmptySlot) {
        if (hadEmptySlot && !hasEmptySlot(inventory))
            inventory.player.sendOverlayMessage(NOW_FULL);
    }
}
