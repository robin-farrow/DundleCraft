package io.github.robin_farrow.dundlecraft.mixin.truedark;

import io.github.robin_farrow.dundlecraft.modules.truedark.lightburnout.LightBurnout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WallTorchBlock.class)
public abstract class WallTorchBlockMixin {

    // WallTorchBlock defines its own state list without calling super, so TorchBlockMixin's version never reaches it
    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void dundlecraft$addBurn(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(LightBurnout.BURN);
    }
}