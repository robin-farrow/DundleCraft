package io.github.robin_farrow.dundlecraft.core;

import net.neoforged.bus.api.IEventBus;

public abstract class DundleModule
{
    public abstract void register(IEventBus modEventBus);
}