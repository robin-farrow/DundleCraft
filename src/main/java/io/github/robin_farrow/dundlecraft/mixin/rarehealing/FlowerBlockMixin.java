package io.github.robin_farrow.dundlecraft.mixin.rarehealing;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(FlowerBlock.class)
public abstract class FlowerBlockMixin
{
    private static final SuspiciousStewEffects OXEYE_DAISY_EFFECTS = new SuspiciousStewEffects(
            List.of(new SuspiciousStewEffects.Entry(MobEffects.ABSORPTION, 5 * 20)));

    private static final SuspiciousStewEffects ONE_TICK_SATURATION = new SuspiciousStewEffects(
            List.of(new SuspiciousStewEffects.Entry(MobEffects.SATURATION, 1)));

    // Mooshroom stews give the same effects as the crafting recipes
    @ModifyReturnValue(method = "getSuspiciousEffects", at = @At("RETURN"))
    private SuspiciousStewEffects dundlecraft$customStewEffects(SuspiciousStewEffects original) {
        Object self = this;
        if (self == Blocks.OXEYE_DAISY) return OXEYE_DAISY_EFFECTS;
        if (self == Blocks.DANDELION || self == Blocks.BLUE_ORCHID) return ONE_TICK_SATURATION;
        return original;
    }
}
