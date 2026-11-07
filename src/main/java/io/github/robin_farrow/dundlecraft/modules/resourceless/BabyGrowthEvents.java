package io.github.robin_farrow.dundlecraft.modules.resourceless;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public class BabyGrowthEvents
{
    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event)
    {
        if (event.getLevel().isClientSide() || event.loadedFromDisk()) return;

        // Exactly -24000 means a brand-new baby; anything else (e.g. a baby walking through a portal) is left alone
        if (event.getEntity() instanceof Animal animal && animal.getAge() == AgeableMob.BABY_START_AGE) {
            animal.setAge(-ResourceLessValues.BABY_GROWTH);
        }
    }
}
