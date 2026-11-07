package io.github.robin_farrow.dundlecraft.mixin.resourceless;

import io.github.robin_farrow.dundlecraft.modules.resourceless.ResourceLessValues;
import net.minecraft.world.level.block.SaplingBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(SaplingBlock.class)
public abstract class SaplingBlockMixin {
    @ModifyConstant(method = "randomTick", constant = @Constant(intValue = 7))
    private int dundlecraft$slowSapling(int original) {
        return original * ResourceLessValues.TREE_GROWTH_SLOWDOWN;
    }
}
