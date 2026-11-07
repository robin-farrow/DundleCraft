package io.github.robin_farrow.dundlecraft.mixin.dundlehardcore;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.entity.monster.spider.Spider$SpiderTargetGoal")
public abstract class SpiderTargetGoalMixin
{
    // Light no longer stops spiders from picking a target
    @ModifyExpressionValue(method = "canUse",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getLightLevelDependentMagicValue()F"))
    private float dundlecraft$alwaysDark(float brightness) {
        return 0.0F;
    }
}
