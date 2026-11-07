package io.github.robin_farrow.dundlecraft.mixin.resourceless;

import io.github.robin_farrow.dundlecraft.modules.resourceless.ResourceLessValues;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin
{
    // Vanilla lure wait is nextInt(100, 600) ticks
    @ModifyConstant(method = "catchingFish", constant = @Constant(intValue = 100))
    private int dundlecraft$lureMin(int vanilla) {
        return ResourceLessValues.FISHING_LURE_MIN;
    }

    @ModifyConstant(method = "catchingFish", constant = @Constant(intValue = 600))
    private int dundlecraft$lureMax(int vanilla) {
        return ResourceLessValues.FISHING_LURE_MAX;
    }
}
