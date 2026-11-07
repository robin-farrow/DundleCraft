package io.github.robin_farrow.dundlecraft.modules.rarehealing;

import io.github.robin_farrow.dundlecraft.DundleCraft;
import io.github.robin_farrow.dundlecraft.core.DundleModule;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RareHealingModule extends DundleModule
{
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DundleCraft.MODID);

    @Override
    public void register(IEventBus modEventBus)
    {
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addToCreativeTab);
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event)
    {
        MinecraftServer server = event.getServer();
        server.getGameRules().set(GameRules.NATURAL_HEALTH_REGENERATION, false, server);
    }

    // Edible: Fills hunger and saturation, always edible, 7 seconds of regeneration, leaves the bowl
    public static final DeferredItem<Item> STEW_OF_REGENERATION = ITEMS.registerSimpleItem("stew_of_regeneration", properties -> properties
            .stacksTo(1)
            .food(new FoodProperties.Builder().nutrition(20).saturationModifier(0.5F).alwaysEdible().build(),
                    Consumables.defaultFood()
                            .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.REGENERATION, 7 * 20)))
                            .build())
            .usingConvertsTo(Items.BOWL)
            .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));

    private void addToCreativeTab(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS)
        {
            event.accept(STEW_OF_REGENERATION);
        }
    }
}
