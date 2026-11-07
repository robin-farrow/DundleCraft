package io.github.robin_farrow.dundlecraft.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.level.storage.LevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Hud.class)
public abstract class HudMixin
{
    @WrapOperation(
            method = "extractHearts",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelData;isHardcore()Z"))
    private boolean dundlecraft$hardcoreHearts(LevelData instance, Operation<Boolean> original) {
        return true;
    }
}
