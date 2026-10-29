package io.github.robin_farrow.dundlecraft.core;

import net.neoforged.neoforge.common.NeoForge;

public abstract class DundleModule
{
    protected final void registerEvents(Object handler)
    {
        NeoForge.EVENT_BUS.register(handler);
    }
}