package io.github.robin_farrow.dundlecraft.mixin.persistentblocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.ShulkerBoxStorage;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HorseInventoryMenu.class)
public abstract class HorseInventoryMenuMixin
{
    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/world/inventory/Slot"))
    private Slot dundlecraft$noShulkerBoxes(Container container, int slot, int x, int y, Operation<Slot> original) {
        return new Slot(container, slot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !ShulkerBoxStorage.isShulkerBox(stack) && super.mayPlace(stack);
            }
        };
    }
}
