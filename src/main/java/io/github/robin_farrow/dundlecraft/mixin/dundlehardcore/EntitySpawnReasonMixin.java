package io.github.robin_farrow.dundlecraft.mixin.dundlehardcore;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.EntitySpawnReason;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntitySpawnReason.class)
public abstract class EntitySpawnReasonMixin
{
    // Mob spawners ignore light like trial spawners do
    @ModifyReturnValue(method = "ignoresLightRequirements", at = @At("RETURN"))
    private static boolean dundlecraft$spawnersIgnoreLight(boolean original, EntitySpawnReason reason) {
        return original || reason == EntitySpawnReason.SPAWNER;
    }
}
