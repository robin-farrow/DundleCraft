package io.github.robin_farrow.dundlecraft.modules.signalflare;

import io.github.robin_farrow.dundlecraft.DundleCraft;
import io.github.robin_farrow.dundlecraft.core.DundleModule;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SignalFlareModule extends DundleModule
{
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DundleCraft.MODID);

    public static final DeferredItem<SignalFlareItem> SIGNAL_FLARE =
            ITEMS.registerItem("signal_flare", SignalFlareItem::new);

    @Override
    public void register(IEventBus modEventBus)
    {
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addToCreativeTab);
    }

    // So you can pick up all 16 colours in creative for testing
    private void addToCreativeTab(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            for (DyeColor dye : DyeColor.values()) {
                event.accept(flare(dye));
            }
        }
    }

    private static ItemStack flare(DyeColor dye)
    {
        ItemStack stack = new ItemStack(SIGNAL_FLARE.get());
        stack.set(DataComponents.FIREWORK_EXPLOSION, new FireworkExplosion(
                FireworkExplosion.Shape.LARGE_BALL, IntList.of(dye.getFireworkColor()), IntList.of(), false, true));
        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.dundlecraft.signal_flare",
                        Component.translatable("color.minecraft." + dye.getName()))
                .withStyle(style -> style.withColor(SignalFlareItem.brighten(dye.getFireworkColor()))));
        stack.set(DataComponents.TOOLTIP_DISPLAY,
                TooltipDisplay.DEFAULT.withHidden(DataComponents.FIREWORK_EXPLOSION, true));
        return stack;
    }
}
