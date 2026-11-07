package io.github.robin_farrow.dundlecraft.mixin.persistentblocks;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.PersistentBlocksRules;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.ShulkerBoxStorage;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin
{
    @ModifyReturnValue(method = "canPlaceItemInContainer", at = @At("RETURN"))
    private static boolean dundlecraft$storageRules(boolean original, Container container, ItemStack stack, int slot, @Nullable Direction direction) {
        return original
                && !(PersistentBlocksRules.cantStorePersistent(container) && PersistentBlocksRules.isPersistent(stack))
                && !ShulkerBoxStorage.refuses(container, stack);
    }
}
