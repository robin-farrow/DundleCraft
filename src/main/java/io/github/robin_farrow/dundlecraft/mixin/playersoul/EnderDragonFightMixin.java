package io.github.robin_farrow.dundlecraft.mixin.playersoul;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.level.dimension.end.EnderDragonFight;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnderDragonFight.class)
public abstract class EnderDragonFightMixin
{
    // The only read of hasPreviouslyKilledDragon in setDragonKilled is the egg check: make it always "not yet killed"
    @ModifyExpressionValue(method = "setDragonKilled",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/world/level/dimension/end/EnderDragonFight;hasPreviouslyKilledDragon:Z",
                    opcode = Opcodes.GETFIELD))
    private boolean dundlecraft$eggEveryKill(boolean previouslyKilled) {
        return false;
    }
}
