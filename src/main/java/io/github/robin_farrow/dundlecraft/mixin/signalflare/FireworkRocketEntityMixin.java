package io.github.robin_farrow.dundlecraft.mixin.signalflare;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.robin_farrow.dundlecraft.modules.signalflare.SignalFlareModule;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin
{
    // The trail is one addParticle call per tick. For a flare, swap the white spark for a coloured dust particle.
    @WrapOperation(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private void dundlecraft$colouredTrail(Level level, ParticleOptions particle,
                                           double x, double y, double z, double xd, double yd, double zd,
                                           Operation<Void> original)
    {
        ItemStack stack = ((FireworkRocketEntity) (Object) this).getItem();
        FireworkExplosion explosion = stack.get(DataComponents.FIREWORK_EXPLOSION);
        if (stack.is(SignalFlareModule.SIGNAL_FLARE.get()) && explosion != null && !explosion.colors().isEmpty()) {
            particle = new DustParticleOptions(explosion.colors().getInt(0), 1.2F);
        }
        original.call(level, particle, x, y, z, xd, yd, zd);
    }
}