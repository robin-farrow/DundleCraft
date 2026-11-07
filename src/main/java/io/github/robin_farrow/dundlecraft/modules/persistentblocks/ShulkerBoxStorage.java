package io.github.robin_farrow.dundlecraft.modules.persistentblocks;

import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ShulkerBoxStorage
{
    private ShulkerBoxStorage() {}

    public static boolean isShulkerBox(ItemStack stack) {
        return Block.byItem(stack.getItem()) instanceof ShulkerBoxBlock;
    }

    // Containers that keep items, as opposed to crafting grids, anvils and the player's own inventory
    public static boolean isStorage(Container container) {
        return container instanceof BlockEntity   // chests, barrels, hoppers, droppers, furnaces, crafters...
                || container instanceof CompoundContainer  // double chests
                || container instanceof PlayerEnderChestContainer
                || container instanceof ContainerEntity;  // chest and hopper minecarts, chest boats
    }

    public static boolean refuses(Container container, ItemStack stack) {
        return isShulkerBox(stack) && isStorage(container);
    }
}
