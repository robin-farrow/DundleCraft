package io.github.robin_farrow.dundlecraft.mixin.persistentblocks;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.HeavyItemHandRefill;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.PersistentFallingHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Inventory.class)
public abstract class InventoryMixin
{
    // Refill hand after dropping an item
    @WrapMethod(method = "removeFromSelected")
    private ItemStack dundlecraft$refillAfterDrop(boolean all, Operation<ItemStack> original) {
        Inventory inventory = (Inventory) (Object) this;
        Component problem = PersistentFallingHandler.throwProblem(inventory.player, inventory.getSelectedItem());
        if (problem != null) {
            inventory.player.sendOverlayMessage(problem);
            return ItemStack.EMPTY;
        }

        ItemStack removed = original.call(all);
        HeavyItemHandRefill.refill(inventory, inventory.getSelectedSlot(), removed);
        return removed;
    }
}
