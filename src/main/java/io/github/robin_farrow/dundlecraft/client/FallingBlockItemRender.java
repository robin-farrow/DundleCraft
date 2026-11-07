package io.github.robin_farrow.dundlecraft.client;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

public interface FallingBlockItemRender
{
    ItemStackRenderState dundlecraft$item();

    // Blocks drawn only by a block entity renderer, with no block model of their own
    static boolean drawnAsItem(BlockState state) {
        Block block = state.getBlock();
        return block instanceof AbstractChestBlock
                || block instanceof ShulkerBoxBlock
                || block instanceof DecoratedPotBlock;
    }
}
