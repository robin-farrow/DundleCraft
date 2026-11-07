package io.github.robin_farrow.dundlecraft.mixin.resourceless;

import io.github.robin_farrow.dundlecraft.modules.resourceless.BlastFurnaceFuel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipePropertySet;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlastFurnaceMenu.class)
public abstract class BlastFurnaceMenuMixin extends AbstractFurnaceMenu
{
    protected BlastFurnaceMenuMixin(MenuType<?> menuType, ResourceKey<RecipePropertySet> allowedInputs,
                                    RecipeBookType recipeBookType, int containerId, Inventory inventory) {
        super(menuType, allowedInputs, recipeBookType, containerId, inventory);
    }

    @Override
    protected boolean isFuel(ItemStack stack) {
        return BlastFurnaceFuel.allowed(stack) && super.isFuel(stack);
    }
}
