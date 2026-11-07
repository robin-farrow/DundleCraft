package io.github.robin_farrow.dundlecraft.modules.persistentblocks;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public final class HeavyCrafting
{
    private static final Component NOT_ENOUGH_SPACE =
            Component.literal("Not enough inventory space").withStyle(ChatFormatting.RED);

    private HeavyCrafting() {}

    // A crafted heavy-block result that needs more than one slot
    public static boolean isOversizedHeavy(ItemStack stack) {
        return stack.getCount() > 1 && PersistentBlocksRules.isHeavy(stack);
    }

    public static void warnNotEnoughSpace(Player player) {
        player.sendOverlayMessage(NOT_ENOUGH_SPACE);
    }

    public static int freeSlots(Player player) {
        int free = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems())
            if (stack.isEmpty()) free++;
        return free;
    }

    // Room for every persistent product as its own stack, counting an empty cursor as one slot
    public static boolean hasRoomFor(Player player, ItemStack result) {
        if (!isOversizedHeavy(result) || !PersistentBlocksRules.isPersistent(result)) return true;
        int cursor = player.containerMenu.getCarried().isEmpty() ? 1 : 0;
        return freeSlots(player) + cursor >= result.getCount();
    }

    // Leaves 1 in the taken stack and moves the rest into the inventory, then the cursor
    public static void spread(Player player, ItemStack taken) {
        if (!isOversizedHeavy(taken)) return;
        while (taken.getCount() > 1) {
            ItemStack one = taken.split(1);
            if (player.getInventory().add(one)) continue;
            AbstractContainerMenu menu = player.containerMenu;
            if (menu.getCarried().isEmpty()) menu.setCarried(one);
            else player.drop(one, false, Prediction.PREDICTED);   // safety net, shouldn't happen
        }
    }
}
