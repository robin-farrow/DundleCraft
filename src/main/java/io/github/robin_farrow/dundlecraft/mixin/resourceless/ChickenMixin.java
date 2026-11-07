package io.github.robin_farrow.dundlecraft.mixin.resourceless;

import io.github.robin_farrow.dundlecraft.modules.resourceless.ResourceLessValues;
import net.minecraft.world.entity.animal.chicken.Chicken;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Chicken.class)
public abstract class ChickenMixin
{
    // Vanilla: eggTime = random.nextInt(6000) + 6000, in the constructor and after each egg.
    // Both 6000s become 36000, giving 30–60 minutes with an average of 45 mins.
    @ModifyConstant(method = {"<init>", "aiStep"}, constant = @Constant(intValue = 6000))
    private int dundlecraft$eggTimer(int vanilla) {
        return ResourceLessValues.EGG_LAY_BASE;
    }
}
