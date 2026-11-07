package io.github.robin_farrow.dundlecraft.mixin.persistentblocks;

import io.github.robin_farrow.dundlecraft.modules.persistentblocks.HeavyCrafting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin
{
    @Inject(method = "onTake", at = @At("TAIL"))
    private void dundlecraft$spreadHeavyResult(Player player, ItemStack carried, CallbackInfo ci) {
        HeavyCrafting.spread(player, carried);
    }
}
