package io.github.robin_farrow.dundlecraft.mixin.persistentblocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.PersistentBlocksRules;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShelfBlock.class)
public abstract class ShelfBlockMixin
{
    // Persistent hotbar items stay in the hotbar; that shelf slot keeps its item
    @WrapOperation(method = "swapHotbar", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/ShelfBlockEntity;swapItemNoUpdate(ILnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack dundlecraft$keepHeavyOffShelf(ShelfBlockEntity shelf, int slot, ItemStack placed, Operation<ItemStack> original) {
        return PersistentBlocksRules.isPersistent(placed) ? placed : original.call(shelf, slot, placed);
    }
}
