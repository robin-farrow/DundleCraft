package io.github.robin_farrow.dundlecraft.client;

import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;

public final class DundleHardcoreClientEvents
{
    @SubscribeEvent
    public void onRenderNameTag(RenderNameTagEvent.CanRender event)
    {
        if (event.getEntity() instanceof Player) {
            event.setCanRender(TriState.FALSE);
        }
    }
}
