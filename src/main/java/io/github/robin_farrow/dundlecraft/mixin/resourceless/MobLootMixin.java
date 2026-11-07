package io.github.robin_farrow.dundlecraft.mixin.resourceless;

import io.github.robin_farrow.dundlecraft.modules.resourceless.MobLootRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobLootMixin
{
    // Cancel loot table drops if not killed by a player or spawned by a mob spawner
    @Inject(method = "dropFromLootTable", at = @At("HEAD"), cancellable = true)
    private void dundlecraft$limitLoot(ServerLevel level, DamageSource source, boolean playerKilled,
                                       CallbackInfo ci) {
        if (!MobLootRules.allowsLoot((Mob) (Object) this, playerKilled)) {
            ci.cancel();
        }
    }

    // Natural (spawned-in) gear only drops on a player kill, and never from mob spawners.
    // Picked-up gear is unaffected: vanilla still drops it through its separate "preserved" flag.
    @ModifyVariable(method = "dropCustomDeathLoot", at = @At("HEAD"), argsOnly = true)
    private boolean dundlecraft$noSpawnerGear(boolean killedByPlayer) {
        return MobLootRules.allowsLoot((Mob) (Object) this, killedByPlayer);
    }
}
