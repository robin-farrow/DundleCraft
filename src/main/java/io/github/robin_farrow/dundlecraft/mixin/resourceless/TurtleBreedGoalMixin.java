package io.github.robin_farrow.dundlecraft.mixin.resourceless;

import io.github.robin_farrow.dundlecraft.modules.resourceless.ResourceLessValues;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "net.minecraft.world.entity.animal.turtle.Turtle$TurtleBreedGoal")
public abstract class TurtleBreedGoalMixin
{
    @ModifyConstant(method = "breed", constant = @Constant(intValue = 6000))
    private int dundlecraft$breedCooldown(int vanilla) {
        return ResourceLessValues.BREED_COOLDOWN;
    }
}
