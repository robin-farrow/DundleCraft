package io.github.robin_farrow.dundlecraft.mixin.dundlehardcore;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.entity.monster.spider.Spider$SpiderAttackGoal")
public abstract class SpiderAttackGoalMixin
{
    // Light no longer makes spiders randomly give up an attack
    @ModifyExpressionValue(method = "canContinueToUse",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/PathfinderMob;getLightLevelDependentMagicValue()F"))
    private float dundlecraft$alwaysDark(float brightness) {
        return 0.0F;
    }
}
