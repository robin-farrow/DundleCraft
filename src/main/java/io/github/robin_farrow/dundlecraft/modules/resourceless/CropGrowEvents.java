package io.github.robin_farrow.dundlecraft.modules.resourceless;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;

public class CropGrowEvents
{
    @SubscribeEvent
    public void onCropGrow(CropGrowEvent.Pre event)
    {
        // Leave it alone if something else (another mod) already decided
        if (event.getResult() != CropGrowEvent.Pre.Result.DEFAULT) return;

        // Only 1 in CROP_GROWTH_SLOWDOWN growth attempts is allowed through to vanilla's own chance
        if (event.getLevel().getRandom().nextInt(ResourceLessValues.CROP_GROWTH_SLOWDOWN) != 0) {
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
        }
    }
}
