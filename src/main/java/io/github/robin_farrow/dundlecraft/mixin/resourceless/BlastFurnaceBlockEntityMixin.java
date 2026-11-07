package io.github.robin_farrow.dundlecraft.mixin.resourceless;

import io.github.robin_farrow.dundlecraft.modules.resourceless.BlastFurnaceFuel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlastFurnaceBlockEntity.class)
public abstract class BlastFurnaceBlockEntityMixin extends AbstractFurnaceBlockEntity
{
    protected BlastFurnaceBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                           RecipeType<? extends AbstractCookingRecipe> recipeType) {
        super(type, pos, state, recipeType);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        // Slot 1 is the fuel slot
        if (slot == 1 && !BlastFurnaceFuel.allowed(stack)) {
            return false;
        }
        return super.canPlaceItem(slot, stack);
    }
}