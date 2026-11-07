package io.github.robin_farrow.dundlecraft.mixin.resourceless;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.robin_farrow.dundlecraft.modules.resourceless.ResourceLessValues;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Animal.class)
public abstract class AnimalMixin
{
    @ModifyReturnValue(method = "canFallInLove", at = @At("RETURN"))
    private boolean dundlecraft$noBreedLove(boolean original) {
        return original && !dundlecraft$cannotBreed();
    }

    @ModifyReturnValue(method = "isInLove", at = @At("RETURN"))
    private boolean dundlecraft$noBreedInLove(boolean original) {
        return original && !dundlecraft$cannotBreed();
    }

    // Both parents get setAge(6000) here after breeding. This covers almost every animal, including frogs and sniffers.
    @ModifyConstant(method = "finalizeSpawnChildFromBreeding", constant = @Constant(intValue = 6000))
    private int dundlecraft$breedCooldown(int vanilla)
    {
        return ResourceLessValues.BREED_COOLDOWN;
    }

    private boolean dundlecraft$cannotBreed() {
        return (Object) this instanceof Wolf || (Object) this instanceof Cat;
    }
}