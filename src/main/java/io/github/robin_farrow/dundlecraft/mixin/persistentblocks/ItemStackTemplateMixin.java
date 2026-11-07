package io.github.robin_farrow.dundlecraft.mixin.persistentblocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.DataResult;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.PersistentBlocksRules;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStackTemplate.class)
public abstract class ItemStackTemplateMixin
{
    // Vanilla returns an empty stack when a recipe result is bigger than the item's max stack size.
    // For heavy blocks, validate everything except the count, then keep the full stack.
    @WrapOperation(method = "validate",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;validateStrict(Lnet/minecraft/world/item/ItemStack;)Lcom/mojang/serialization/DataResult;"))
    private DataResult<ItemStack> dundlecraft$allowOversizedPersistentBlocks(ItemStack stack, Operation<DataResult<ItemStack>> original)
    {
        if (PersistentBlocksRules.isHeavy(stack)) {
            return original.call(stack.copyWithCount(1)).map(checked -> stack);
        }
        return original.call(stack);
    }
}