package io.github.robin_farrow.dundlecraft.modules.resourceless;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

public class MobLootEvents
{
    // No xp from spawner mobs (except trial spawners)
    @SubscribeEvent
    public void onExperienceDrop(LivingExperienceDropEvent event)
    {
        if (event.getEntity() instanceof Mob mob && MobLootRules.fromMobSpawner(mob))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onSpawnerBroken(BlockDropsEvent event)
    {
        if (!(event.getBlockEntity() instanceof SpawnerBlockEntity spawnerBlock))
        {
            return;
        }

        ServerLevel level = event.getLevel();
        BlockPos pos = event.getPos();
        if (!(spawnerBlock.getSpawner().getOrCreateDisplayEntity(level, pos) instanceof Mob mob)
                || mob instanceof Blaze)
        {
            return;
        }

        Player player = event.getBreaker() instanceof Player p ? p : null;
        DamageSource source = player != null
                ? level.damageSources().playerAttack(player)
                : level.damageSources().generic();
        if (player != null)
        {
            mob.setLastHurtByPlayer(player, 100); // so "killed by player" loot conditions pass
        }

        mob.getLootTable().ifPresent(table ->
        {
            for (int i = 0; i < MobLootRules.SPAWNER_LOOT_ROLLS; i++)
            {
                mob.dropFromLootTable(level, source, player != null, table, stack ->
                {
                    ItemEntity item = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                    item.setDefaultPickUpDelay();
                    event.getDrops().add(item);
                });
            }
        });

        event.setDroppedExperience(
                mob.getExperienceReward(level, player) * MobLootRules.SPAWNER_LOOT_ROLLS);
    }
}
