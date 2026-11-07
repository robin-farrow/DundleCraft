package io.github.robin_farrow.dundlecraft.mixin.persistentblocks;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;

@Mixin(EntityCollisionContext.class)
public abstract class EntityCollisionContextMixin
{
    @Shadow
    public abstract @Nullable Entity getEntity();

    // Falling blocks treat portals as full blocks, so they land on them or bounce off beside them
    @ModifyReturnValue(method = "getCollisionShape", at = @At("RETURN"))
    private VoxelShape dundlecraft$solidPortalsForFallingBlocks(VoxelShape original, BlockState state, CollisionGetter collisionGetter, BlockPos pos) {
        return getEntity() instanceof FallingBlockEntity && state.is(BlockTags.PORTALS) ? Shapes.block() : original;
    }
}
