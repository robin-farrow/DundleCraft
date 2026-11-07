package io.github.robin_farrow.dundlecraft.modules.dundlehardcore;

import io.github.robin_farrow.dundlecraft.DundleCraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

public final class PhantomEvents
{
    private static final Identifier END_PHANTOM_HEALTH_ID = Identifier.fromNamespaceAndPath(DundleCraft.MODID, "end_phantom_health");
    private static final int END_PHANTOM_MAX_SIZE = 6; // 1.9x body size

    // Phantoms spawning naturally in the End start 20-56 blocks above the ground, so they circle high
    @SubscribeEvent
    public void onEndPhantomSpawn(FinalizeSpawnEvent event)
    {
        if (!(event.getEntity() instanceof Phantom phantom) || event.getSpawnType() != EntitySpawnReason.NATURAL) return;
        ServerLevel level = event.getLevel().getLevel();
        if (level.dimension() != Level.END) return;

        int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING, phantom.getBlockX(), phantom.getBlockZ());
        double y = Math.max(ground, phantom.getY()) + 20 + level.getRandom().nextInt(37);
        y = Math.min(y, level.getMaxY() - 2);

        double lift = y - phantom.getY();
        if (level.noCollision(phantom, phantom.getBoundingBox().move(0, lift, 0)))
            phantom.setPos(phantom.getX(), y, phantom.getZ());
    }

    // Naturally spawned End phantoms come in random sizes, 0 to END_PHANTOM_MAX_SIZE
    @SubscribeEvent
    public void onEndPhantomJoin(EntityJoinLevelEvent event)
    {
        if (!(event.getEntity() instanceof Phantom phantom) || event.loadedFromDisk()) return;
        if (!(event.getLevel() instanceof ServerLevel level) || level.dimension() != Level.END) return;
        if (phantom.getSpawnType() != EntitySpawnReason.NATURAL) return;

        phantom.setPhantomSize(level.getRandom().nextInt(END_PHANTOM_MAX_SIZE + 1));

        // End phantoms get double health on top of the doubling every mob gets
        AttributeInstance maxHealth = phantom.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null && !maxHealth.hasModifier(END_PHANTOM_HEALTH_ID)) {
            maxHealth.addPermanentModifier(new AttributeModifier(END_PHANTOM_HEALTH_ID, 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            phantom.setHealth(phantom.getMaxHealth());
        }
    }
}
