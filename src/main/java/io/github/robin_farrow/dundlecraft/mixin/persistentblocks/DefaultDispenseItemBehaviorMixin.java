package io.github.robin_farrow.dundlecraft.mixin.persistentblocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.PersistentFallingHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DefaultDispenseItemBehavior.class)
public abstract class DefaultDispenseItemBehaviorMixin
{
    // Set when a persistent block couldn't be dispensed, so dispense() plays the failure click and no smoke
    @Unique
    private boolean dundlecraft$obstructed;

    @Inject(method = "dispense", at = @At("HEAD"))
    private void dundlecraft$resetObstructed(BlockSource source, ItemStack dispensed, CallbackInfoReturnable<ItemStack> cir) {
        dundlecraft$obstructed = false;
    }

    // Every dispenser and dropper ends up here when it spits an item out, including special behaviours falling back to it
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private void dundlecraft$dispensePersistentBlock(BlockSource source, ItemStack dispensed, CallbackInfoReturnable<ItemStack> cir) {
        BlockState falling = PersistentFallingHandler.fallingStateFor(dispensed);
        if (falling == null) return;

        ServerLevel level = source.level();
        Direction facing = source.state().getValue(DispenserBlock.FACING);
        BlockPos front = source.pos().relative(facing);

        // Something solid against the face: fail, keeping the block
        if (!PersistentFallingHandler.isDestructible(level, front)) {
            dundlecraft$obstructed = true;
            cir.setReturnValue(dispensed);
            return;
        }

        // Droppers let it fall from the front block; dispensers fire it from there
        Vec3 velocity = source.state().getBlock() instanceof DropperBlock
                ? Vec3.ZERO
                : PersistentFallingHandler.dispenserVelocity(level.getRandom(), facing);
        PersistentFallingHandler.spawn(level, Vec3.atBottomCenterOf(front), PersistentFallingHandler.randomOrientation(falling, level.getRandom()), velocity, null);
        dispensed.shrink(1);
        cir.setReturnValue(dispensed);
    }

    @WrapOperation(method = "dispense", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/dispenser/DefaultDispenseItemBehavior;playSound(Lnet/minecraft/core/dispenser/BlockSource;)V"))
    private void dundlecraft$failureClick(DefaultDispenseItemBehavior self, BlockSource source, Operation<Void> original) {
        if (dundlecraft$obstructed) source.level().levelEvent(1001, source.pos(), 0);
        else original.call(self, source);
    }

    @WrapOperation(method = "dispense", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/dispenser/DefaultDispenseItemBehavior;playAnimation(Lnet/minecraft/core/dispenser/BlockSource;Lnet/minecraft/core/Direction;)V"))
    private void dundlecraft$noSmokeWhenObstructed(DefaultDispenseItemBehavior self, BlockSource source, Direction direction, Operation<Void> original) {
        if (!dundlecraft$obstructed) original.call(self, source, direction);
    }
}