package io.github.robin_farrow.dundlecraft.mixin.persistentblocks;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.PersistentBlocksRules;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.HeavyItemHandRefill;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin
{
    // Refill the hand after placing a heavy block, from either hand
    @WrapMethod(method = "useOn")
    private InteractionResult dundlecraft$refillAfterPlace(UseOnContext context, Operation<InteractionResult> original) {
        ItemStack self = (ItemStack) (Object) this;
        if (!PersistentBlocksRules.isHeavy(self)) return original.call(context);

        ItemStack used = self.copyWithCount(1);
        InteractionResult result = original.call(context);

        Player player = context.getPlayer();
        if (player != null && self.isEmpty() && player.getItemInHand(context.getHand()) == self) {
            Inventory inventory = player.getInventory();
            int slot = context.getHand() == InteractionHand.MAIN_HAND ? inventory.getSelectedSlot() : Inventory.SLOT_OFFHAND;
            HeavyItemHandRefill.refill(inventory, slot, used);
        }
        return result;
    }
}
