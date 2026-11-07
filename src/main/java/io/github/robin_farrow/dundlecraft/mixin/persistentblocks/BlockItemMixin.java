package io.github.robin_farrow.dundlecraft.mixin.persistentblocks;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.PersistentBlocksRules;
import net.minecraft.world.item.BlockItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Shulker box slots, hoppers feeding shulker boxes and bundles all ask the item canFitInsideContainerItems()
@Mixin(BlockItem.class)
public abstract class BlockItemMixin
{
    @ModifyReturnValue(method = "canFitInsideContainerItems", at = @At("RETURN"))
    private boolean dundlecraft$noHeavyBlocksInContainerItems(boolean original) {
        return original && !PersistentBlocksRules.isPersistent(((BlockItem) (Object) this).getBlock());
    }
}
