package io.github.robin_farrow.dundlecraft.mixin.persistentblocks;

import io.github.robin_farrow.dundlecraft.modules.persistentblocks.DragonEggClaim;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DragonEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DragonEggBlock.class)
public abstract class DragonEggBlockMixin
{
    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void dundlecraft$claimOnUse(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (DragonEggClaim.tryClaim(level, pos, player))
            cir.setReturnValue(InteractionResult.SUCCESS);
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void dundlecraft$claimOnAttack(BlockState state, Level level, BlockPos pos, Player player, CallbackInfo ci) {
        if (DragonEggClaim.tryClaim(level, pos, player))
            ci.cancel();
    }
}
