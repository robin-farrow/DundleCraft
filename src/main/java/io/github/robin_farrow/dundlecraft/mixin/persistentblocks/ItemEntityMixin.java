package io.github.robin_farrow.dundlecraft.mixin.persistentblocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.InventoryFullWarning;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin
{
    @WrapOperation(method = "playerTouch", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean dundlecraft$warnInventoryFull(Inventory inventory, ItemStack stack, Operation<Boolean> original) {
        boolean hadEmptySlot = InventoryFullWarning.hasEmptySlot(inventory);
        boolean added = original.call(inventory, stack);
        InventoryFullWarning.warnIfJustFilled(inventory, hadEmptySlot);
        return added;
    }
}
