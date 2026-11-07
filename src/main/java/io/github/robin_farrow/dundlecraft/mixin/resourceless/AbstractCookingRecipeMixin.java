package io.github.robin_farrow.dundlecraft.mixin.resourceless;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.robin_farrow.dundlecraft.modules.resourceless.FurnaceFuelValues;
import io.github.robin_farrow.dundlecraft.modules.resourceless.ResourceLessModule;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractCookingRecipe.class)
public abstract class AbstractCookingRecipeMixin {
    @ModifyReturnValue(method = "cookingTime", at = @At("RETURN"))
    private int dundlecraft$fixedCookingTime(int original) {
        return (Object) this instanceof CampfireCookingRecipe
                ? FurnaceFuelValues.CAMPFIRE_COOK_TICKS
                : FurnaceFuelValues.FURNACE_COOK_TICKS;
    }
}