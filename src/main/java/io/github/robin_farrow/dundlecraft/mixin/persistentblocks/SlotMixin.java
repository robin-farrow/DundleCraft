package io.github.robin_farrow.dundlecraft.mixin.persistentblocks;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.HeavyCrafting;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.PersistentBlocksRules;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.ShulkerBoxStorage;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Slot.class)
public abstract class SlotMixin
{
    @Shadow
    @Final
    public Container container;

    @Shadow
    public abstract ItemStack getItem();

    @ModifyReturnValue(method = "mayPickup", at = @At("RETURN"))
    private boolean dundlecraft$needsRoomForHeavyResult(boolean original, Player player) {
        return original && (!(container instanceof ResultContainer) || HeavyCrafting.hasRoomFor(player, getItem()));
    }

    @Inject(method = "onTake", at = @At("TAIL"))
    private void dundlecraft$spreadHeavyResult(Player player, ItemStack carried, CallbackInfo ci) {
        if (container instanceof ResultContainer)
            HeavyCrafting.spread(player, carried);
    }

    @ModifyReturnValue(method = "mayPlace", at = @At("RETURN"))
    private boolean dundlecraft$storageRules(boolean original, ItemStack stack) {
        return original
                && !(PersistentBlocksRules.cantStorePersistent(container) && PersistentBlocksRules.isPersistent(stack))
                && !ShulkerBoxStorage.refuses(container, stack);
    }
}
