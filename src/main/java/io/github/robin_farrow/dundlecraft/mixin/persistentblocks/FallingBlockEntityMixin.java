package io.github.robin_farrow.dundlecraft.mixin.persistentblocks;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.PersistentBlocksRules;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.PersistentFallingHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity
{
    // Never called; Mixin requires it to compile against Entity
    protected FallingBlockEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    // Falls through boats, rafts, happy ghasts and shulkers instead of landing on them
    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }

    // Falling blocks never teleport, even if one ends up inside a portal
    @Override
    public boolean canUsePortal(boolean ignorePassenger) {
        return false;
    }

    // A persistent block only stops on something solid, so it keeps falling over anything without a collider
    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/FallingBlock;isFree(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean dundlecraft$fallOverDestructible(boolean original) {
        FallingBlockEntity self = (FallingBlockEntity) (Object) this;
        if (!PersistentBlocksRules.isPersistent(self.getBlockState().getBlock())) return original;
        return PersistentFallingHandler.isDestructible(self.level(), self.blockPosition().below());
    }

    // The block's own place sound on a normal landing
    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean dundlecraft$place(Level level, BlockPos pos, BlockState state, Operation<Boolean> original) {
        boolean placed = original.call(level, pos, state);
        if (placed && PersistentBlocksRules.isPersistent(state.getBlock()))
            PersistentFallingHandler.playPlaceSound(level, pos, state);
        return placed;
    }

    // Combines with a matching bottom slab, or snaps, instead of breaking into an item
    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/item/FallingBlockEntity;spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private @Nullable ItemEntity dundlecraft$snap(FallingBlockEntity self, ServerLevel level, ItemLike item, Operation<ItemEntity> original) {
        if (!PersistentBlocksRules.isPersistent(self.getBlockState().getBlock()))
            return original.call(self, level, item);
        if (!PersistentFallingHandler.tryCombineSlab(level, self))
            PersistentFallingHandler.snap(level, self);
        return null;
    }
}