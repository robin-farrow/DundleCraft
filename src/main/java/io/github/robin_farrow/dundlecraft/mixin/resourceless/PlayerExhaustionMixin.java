package io.github.robin_farrow.dundlecraft.mixin.resourceless;

import io.github.robin_farrow.dundlecraft.modules.resourceless.ResourceLessValues;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class PlayerExhaustionMixin
{
    @ModifyVariable(method = "causeFoodExhaustion", at = @At("HEAD"), argsOnly = true)
    private float dundlecraft$moreExhaustion(float amount) {
        return amount * ResourceLessValues.PLAYER_EXHAUSTION_MULTIPLIER;
    }
}
