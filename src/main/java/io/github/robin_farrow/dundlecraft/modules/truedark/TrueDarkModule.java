package io.github.robin_farrow.dundlecraft.modules.truedark;

import io.github.robin_farrow.dundlecraft.core.DundleModule;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

public final class TrueDarkModule extends DundleModule
{
    private DayLengthController dayLengthController;

    public TrueDarkModule() {
        registerEvents(this);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event)
    {
        dayLengthController = new DayLengthController(event.getServer());
        NeoForge.EVENT_BUS.register(dayLengthController);
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event)
    {
        if (dayLengthController != null)
        {
            NeoForge.EVENT_BUS.unregister(dayLengthController);
            dayLengthController = null;
        }
    }

    @SubscribeEvent
    public void onCanSleep(CanPlayerSleepEvent event) {
        if (event.getProblem() == null) {
            event.setProblem(new Player.BedSleepingProblem(
                    Component.translatable("dundlecraft.truedark.cant_sleep")));
        }
    }
}
