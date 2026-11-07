package io.github.robin_farrow.dundlecraft.mixin.truedark;

import io.github.robin_farrow.dundlecraft.modules.truedark.lightburnout.LightBurnout;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseTorchBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TorchBlock.class)
public abstract class TorchBlockMixin extends BaseTorchBlock {
    protected TorchBlockMixin(Properties properties) {
        super(properties);
    }

    // Adds the shared burn property to torch, soul torch and copper torch
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LightBurnout.BURN);
    }

    // Vanilla torches don't random tick, so switch it on
    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        LightBurnout.tick(state, level, pos, random,
                () -> level.removeBlock(pos, false));
    }
}