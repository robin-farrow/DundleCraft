package io.github.robin_farrow.dundlecraft.modules.dundlehardcore;

import io.github.robin_farrow.dundlecraft.core.DundleModule;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

public final class DundleHardcoreModule extends DundleModule
{
    private static final float MACE_DAMAGE_CAP = 100.0F;

    @Override
    public void register(IEventBus modEventBus)
    {
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new CancelChatEvents());
        NeoForge.EVENT_BUS.register(new PlayerAttributeEvents());
        NeoForge.EVENT_BUS.register(new EntityIncreasedHealthEvents());
        NeoForge.EVENT_BUS.register(new PhantomEvents());
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event)
    {
        MinecraftServer server = event.getServer();
        server.setDifficulty(Difficulty.HARD, true);
        server.setDifficultyLocked(true);

        GameRules rules = server.getGameRules();
        rules.set(GameRules.REDUCED_DEBUG_INFO, true, server);
        rules.set(GameRules.LOCATOR_BAR, false, server);
    }

    @SubscribeEvent
    public void onIncomingDamage(LivingIncomingDamageEvent event)
    {
        ItemStack weapon = event.getSource().getWeaponItem();
        if (weapon != null && weapon.is(Items.MACE) && event.getAmount() > MACE_DAMAGE_CAP) {
            event.setAmount(MACE_DAMAGE_CAP);
        }
    }

    // Spawner monsters ignore the light-based "do I like this spot" check, so spawners work in light level 15
    @SubscribeEvent
    public void onSpawnerPositionCheck(MobSpawnEvent.PositionCheck event)
    {
        if (event.getSpawnType() != EntitySpawnReason.SPAWNER || !(event.getEntity() instanceof Monster mob)) return;

        event.setResult(mob.checkSpawnObstruction(event.getLevel())
                ? MobSpawnEvent.PositionCheck.Result.SUCCEED
                : MobSpawnEvent.PositionCheck.Result.FAIL);
    }
}
