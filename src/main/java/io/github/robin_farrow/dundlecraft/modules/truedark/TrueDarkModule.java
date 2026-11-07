package io.github.robin_farrow.dundlecraft.modules.truedark;

import io.github.robin_farrow.dundlecraft.core.DundleModule;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

public final class TrueDarkModule extends DundleModule
{
    @Override
    public void register(IEventBus modEventBus)
    {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event)
    {
        NeoForge.EVENT_BUS.register(new DayLengthController(event.getServer()));
    }

    @SubscribeEvent
    public void onCanSleep(CanPlayerSleepEvent event)
    {
        event.setProblem(new Player.BedSleepingProblem(
                Component.translatable("dundlecraft.truedark.cant_sleep")));
    }
}
