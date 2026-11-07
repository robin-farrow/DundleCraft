package io.github.robin_farrow.dundlecraft.mixin.persistentblocks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.BulkCraftingGridRefill;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.PersistentBlocksRules;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.HeavyCrafting;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.PersistentFallingHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin
{
    // Q over a slot, or clicking outside the screen, fails if a persistent block has no room to spawn
    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    private void dundlecraft$noThrowWithoutRoom(int slotIndex, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        ItemStack throwing;
        if (containerInput == ContainerInput.THROW && slotIndex >= 0 && menu.getCarried().isEmpty())
            throwing = menu.getSlot(slotIndex).getItem();
        else if (slotIndex == -999 && (containerInput == ContainerInput.PICKUP || containerInput == ContainerInput.QUICK_MOVE))
            throwing = menu.getCarried();
        else
            return;

        Component problem = PersistentFallingHandler.throwProblem(player, throwing);
        if (problem != null) {
            player.sendOverlayMessage(problem);
            ci.cancel();
        }
    }

    // Remembers the recipe and result before a shift-click on a crafting result uses them up
    @Inject(method = "doClick", at = @At("HEAD"))
    private void dundlecraft$rememberShiftCraft(int slotIndex, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci,
                                                @Share("refillRecipe") LocalRef<RecipeHolder<?>> recipe, @Share("refillItem") LocalRef<ItemStack> crafted) {
        if (containerInput != ContainerInput.QUICK_MOVE || !(player instanceof ServerPlayer)) return;

        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        RecipeHolder<?> used = BulkCraftingGridRefill.recipeBeingShiftCrafted(menu, slotIndex);
        if (used == null) return;
        recipe.set(used);
        crafted.set(menu.getSlot(slotIndex).getItem().copy());
    }

    // After the shift-click has finished crafting, refills the grid ready for the next click
    @Inject(method = "doClick", at = @At("TAIL"))
    private void dundlecraft$refillAfterShiftCraft(int slotIndex, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci,
                                                   @Share("refillRecipe") LocalRef<RecipeHolder<?>> recipe, @Share("refillItem") LocalRef<ItemStack> crafted) {
        if (recipe.get() != null && player instanceof ServerPlayer serverPlayer && (Object) this instanceof AbstractCraftingMenu crafting)
            BulkCraftingGridRefill.refill(crafting, serverPlayer, recipe.get(), crafted.get());
    }

    // Disable Q throwing heavy crafting results, and check for enough inventory space
    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    private void dundlecraft$heavyResultClick(int slotIndex, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo ci) {
        if (slotIndex < 0) return;

        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        Slot slot = menu.getSlot(slotIndex);
        ItemStack result = slot.getItem();
        if (!(slot.container instanceof ResultContainer) || !PersistentBlocksRules.isHeavy(result)) return;

        if (containerInput == ContainerInput.THROW) {
            ci.cancel();
            return;
        }

        // Click with an empty cursor, or number key into an empty hotbar or offhand slot
        boolean pickup = containerInput == ContainerInput.PICKUP && (buttonNum == 0 || buttonNum == 1)
                && menu.getCarried().isEmpty();
        boolean swap = containerInput == ContainerInput.SWAP && (buttonNum >= 0 && buttonNum < 9 || buttonNum == 40)
                && player.getInventory().getItem(buttonNum).isEmpty();

        if ((pickup || swap) && !HeavyCrafting.hasRoomFor(player, result)) {
            HeavyCrafting.warnNotEnoughSpace(player);
            ci.cancel();
        }
    }

    @WrapOperation(method = "doClick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;quickMoveStack(Lnet/minecraft/world/entity/player/Player;I)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack dundlecraft$quickMoveHeavyResult(AbstractContainerMenu menu, Player player, int slotIndex, Operation<ItemStack> original) {
        Slot slot = menu.getSlot(slotIndex);
        ItemStack result = slot.getItem();
        if (!(slot.container instanceof ResultContainer) || !HeavyCrafting.isOversizedHeavy(result))
            return original.call(menu, player, slotIndex);

        // Shift-click never uses the cursor, so every persistent product needs an empty slot
        if (PersistentBlocksRules.isPersistent(result) && HeavyCrafting.freeSlots(player) < result.getCount())
        {
            HeavyCrafting.warnNotEnoughSpace(player);
            return ItemStack.EMPTY;
        }

        ItemStack clicked = result.copy();
        ItemStack taken = slot.safeTake(result.getCount(), Integer.MAX_VALUE, player);
        if (!player.getInventory().add(taken))
            player.drop(taken, false, Prediction.PREDICTED);   // heavy-only blocks without room drop instead
        return clicked;
    }
}
