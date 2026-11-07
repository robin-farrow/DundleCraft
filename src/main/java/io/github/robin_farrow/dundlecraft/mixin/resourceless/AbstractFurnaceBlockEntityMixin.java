package io.github.robin_farrow.dundlecraft.mixin.resourceless;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.robin_farrow.dundlecraft.modules.resourceless.BlastFurnaceFuel;
import io.github.robin_farrow.dundlecraft.modules.resourceless.FurnaceFuelValues;
import io.github.robin_farrow.dundlecraft.modules.resourceless.ResourceLessModule;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {
    @ModifyReturnValue(method = "getBurnDuration", at = @At("RETURN"))
    private int dundlecraft$fuelTime(int original, ServerLevel level, ItemStack fuelItem) {
        if ((Object) this instanceof BlastFurnaceBlockEntity && !BlastFurnaceFuel.allowed(fuelItem)) {
            return 0;
        }
        return FurnaceFuelValues.burnTicks(fuelItem, original);
    }
}
