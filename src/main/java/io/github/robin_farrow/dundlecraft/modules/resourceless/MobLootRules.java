package io.github.robin_farrow.dundlecraft.modules.resourceless;

import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Blaze;

public final class MobLootRules {
    public static final int SPAWNER_LOOT_ROLLS = 16;

    private MobLootRules() {}

    /** Mob spawner, except blazes. Trial spawners are a different spawn reason. */
    public static boolean fromMobSpawner(Mob mob) {
        return mob.getSpawnType() == EntitySpawnReason.SPAWNER && !(mob instanceof Blaze);
    }

    /** Loot table drops need a player (or tamed pet) kill, and never come from plain spawners. */
    public static boolean allowsLoot(Mob mob, boolean playerKilled) {
        return playerKilled && !fromMobSpawner(mob);
    }
}