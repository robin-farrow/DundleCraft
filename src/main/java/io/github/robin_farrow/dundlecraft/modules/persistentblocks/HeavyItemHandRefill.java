package io.github.robin_farrow.dundlecraft.modules.persistentblocks;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class HeavyItemHandRefill
{
    private HeavyItemHandRefill() {}

    // Fills an emptied slot with a matching heavy block, searching the hotbar (8 down to 0) first, then the main inventory (35 down to 9)
    public static void refill(Inventory inventory, int slot, ItemStack used) {
        if (used.isEmpty() || !PersistentBlocksRules.isHeavy(used) || !inventory.getItem(slot).isEmpty()) return;

        for (int i = Inventory.SELECTION_SIZE - 1; i >= 0; i--)
            if (tryMove(inventory, i, slot, used)) return;
        for (int i = Inventory.INVENTORY_SIZE - 1; i >= Inventory.SELECTION_SIZE; i--)
            if (tryMove(inventory, i, slot, used)) return;
    }

    private static boolean tryMove(Inventory inventory, int source, int slot, ItemStack used) {
        if (source == slot || source == inventory.getSelectedSlot()) return false;
        ItemStack candidate = inventory.getItem(source);
        if (!ItemStack.isSameItemSameComponents(candidate, used)) return false;
        inventory.setItem(slot, candidate);
        inventory.setItem(source, ItemStack.EMPTY);
        return true;
    }
}
