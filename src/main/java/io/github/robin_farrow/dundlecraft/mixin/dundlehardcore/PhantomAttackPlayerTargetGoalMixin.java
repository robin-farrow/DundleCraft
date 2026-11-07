package io.github.robin_farrow.dundlecraft.mixin.dundlehardcore;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "net.minecraft.world.entity.monster.Phantom$PhantomAttackPlayerTargetGoal")
public abstract class PhantomAttackPlayerTargetGoalMixin
{
    // Phantoms notice players up to 32 blocks away horizontally instead of 16
    @ModifyConstant(method = "canUse", constant = @Constant(doubleValue = 16.0))
    private double dundlecraft$widerSearch(double vanilla) {
        return 32.0;
    }
}
