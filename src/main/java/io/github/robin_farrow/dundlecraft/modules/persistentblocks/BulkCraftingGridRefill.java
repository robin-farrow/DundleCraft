package io.github.robin_farrow.dundlecraft.modules.persistentblocks;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import javax.annotation.Nullable;

public final class BulkCraftingGridRefill
{
    private BulkCraftingGridRefill() {}

    // The recipe a shift-click on a crafting result is making, or null if this isn't one
    public static @Nullable RecipeHolder<?> recipeBeingShiftCrafted(AbstractContainerMenu menu, int slotIndex) {
        if (!(menu instanceof AbstractCraftingMenu crafting) || slotIndex < 0) return null;
        Slot result = crafting.getResultSlot();
        if (menu.getSlot(slotIndex) != result || !(result.container instanceof ResultContainer container)) return null;
        return container.getRecipeUsed();
    }

    // Puts one more set of ingredients in the grid if the recipe can be made again from the inventory
    public static void refill(AbstractCraftingMenu crafting, ServerPlayer player, RecipeHolder<?> recipe, ItemStack crafted) {
        // The grid can still make it: the shift-click stopped for lack of space, not ingredients
        if (ItemStack.isSameItem(crafting.getResultSlot().getItem(), crafted)) return;
        // Special recipes (dyeing, map cloning, fireworks) can't be placed, even by the recipe book
        if (recipe.value().placementInfo().isImpossibleToPlace()) return;

        StackedItemContents available = new StackedItemContents();
        player.getInventory().fillStackedContents(available);
        crafting.fillCraftSlotsStackedContents(available);
        if (!available.canCraft(recipe.value(), null)) return;

        crafting.handlePlacement(false, false, recipe, player.level(), player.getInventory());
    }
}
