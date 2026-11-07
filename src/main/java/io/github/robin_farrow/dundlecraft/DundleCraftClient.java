package io.github.robin_farrow.dundlecraft;

import io.github.robin_farrow.dundlecraft.client.BurnoutItemDecorator;
import io.github.robin_farrow.dundlecraft.client.DundleHardcoreClientEvents;
import io.github.robin_farrow.dundlecraft.client.PersistentItemDecorator;
import io.github.robin_farrow.dundlecraft.client.ItemTooltips;
import io.github.robin_farrow.dundlecraft.modules.persistentblocks.PersistentBlocksRules;
import io.github.robin_farrow.dundlecraft.modules.playersoul.SoulClientHud;
import io.github.robin_farrow.dundlecraft.modules.truedark.lightburnout.LightBurnout;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.tooltip.TooltipLocation;
import net.neoforged.neoforge.event.RegisterTooltipAppendersEvent;

@Mod(value = DundleCraft.MODID, dist = Dist.CLIENT)
public class DundleCraftClient
{
    public DundleCraftClient(IEventBus modEventBus, ModContainer container)
    {
        NeoForge.EVENT_BUS.register(new DundleHardcoreClientEvents());

        modEventBus.addListener((RegisterGuiLayersEvent event) ->
                event.registerBelow(VanillaGuiLayers.PLAYER_HEALTH, SoulClientHud.LAYER_ID, SoulClientHud::render));

        modEventBus.addListener(this::registerItemDecorators);
        modEventBus.addListener(this::registerTooltipAppenders);
    }

    // Placed just under each item's own description, above enchantments and the F3 + H item info
    private void registerTooltipAppenders(RegisterTooltipAppendersEvent event)
    {
        event.registerAppender(TooltipLocation.POST_CUSTOM, ItemTooltips::soul);
        event.registerAppender(TooltipLocation.POST_CUSTOM, ItemTooltips::burnout);
        event.registerAppender(TooltipLocation.POST_CUSTOM, ItemTooltips::persistent);
    }

    private void registerItemDecorators(RegisterItemDecorationsEvent event)
    {
        PersistentItemDecorator persistent = new PersistentItemDecorator();
        BurnoutItemDecorator burnout = new BurnoutItemDecorator();

        for (Item item : BuiltInRegistries.ITEM) {
            if (!(item instanceof BlockItem blockItem)) continue;
            if (PersistentBlocksRules.isPersistent(blockItem.getBlock()))
                event.register(item, persistent);
            if (blockItem.getBlock().defaultBlockState().hasProperty(LightBurnout.BURN))
                event.register(item, burnout);
        }
    }
}
