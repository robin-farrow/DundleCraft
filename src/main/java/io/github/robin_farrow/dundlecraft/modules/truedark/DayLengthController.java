package io.github.robin_farrow.dundlecraft.modules.truedark;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class DayLengthController
{
    private static final long NIGHT_START = 13000;
    private static final long NIGHT_END = 23000;
    private static final float DAY_RATE = 0.5f;
    private static final float NIGHT_RATE = 1.0f;

    private final MinecraftServer server;
    private final Holder<WorldClock> overworld;

    public DayLengthController (MinecraftServer server)
    {
        this.server = server;
        this.overworld = server.registryAccess()
                .lookupOrThrow(Registries.WORLD_CLOCK)
                .getOrThrow(WorldClocks.OVERWORLD);
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event)
    {
        if (server.getTickCount() % 10 != 0) return;

        ServerClockManager clocks = server.clockManager();
        long time = clocks.getInstance(overworld).totalTicks() % 24000;
        boolean night = time >= NIGHT_START && time < NIGHT_END;
        float wanted = night ? NIGHT_RATE : DAY_RATE;

        if (clocks.getInstance(overworld).rate() != wanted)
        {
            clocks.setRate(overworld, wanted);
        }
    }
}