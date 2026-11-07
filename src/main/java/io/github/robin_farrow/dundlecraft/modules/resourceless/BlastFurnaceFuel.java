package io.github.robin_farrow.dundlecraft.modules.resourceless;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class BlastFurnaceFuel {
    private BlastFurnaceFuel() {}

    public static boolean allowed(ItemStack stack) {
        return stack.is(Items.COAL)
                || stack.is(Items.CHARCOAL)
                || stack.is(Items.BLAZE_ROD)
                || stack.is(Items.LAVA_BUCKET)
                || stack.is(Items.COAL_BLOCK);
    }
}
