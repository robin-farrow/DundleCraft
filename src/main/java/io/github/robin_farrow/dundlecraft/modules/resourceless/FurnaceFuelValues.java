package io.github.robin_farrow.dundlecraft.modules.resourceless;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class FurnaceFuelValues
{
    public static final int FURNACE_COOK_TICKS = 60 * 20;
    public static final int CAMPFIRE_COOK_TICKS = 8 * 60 * 20;
    public static final double FURNACE_FUEL_VALUE = 0.5;
    private static final int ITEMS_PER_LAVA_BUCKET_FUEL = 16;
    private static final int LEAF_LITTER_FUEL_PER_ITEM = 16;

    private static final int VANILLA_COOK_TICKS = 200;
    private static final double FUEL_MULTIPLIER =
            (double) FURNACE_COOK_TICKS / VANILLA_COOK_TICKS * FURNACE_FUEL_VALUE;

    public static int burnTicks(ItemStack stack, int original) {
        if (stack.is(Items.LAVA_BUCKET)) {
            return ITEMS_PER_LAVA_BUCKET_FUEL * FURNACE_COOK_TICKS;
        }
        if (stack.is(Items.LEAF_LITTER)) {
            return FURNACE_COOK_TICKS / LEAF_LITTER_FUEL_PER_ITEM;
        }
        return original <= 0 ? original : (int) Math.round(original * FUEL_MULTIPLIER);
    }
}
