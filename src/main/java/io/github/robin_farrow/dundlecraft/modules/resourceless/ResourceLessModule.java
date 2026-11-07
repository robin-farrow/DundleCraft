package io.github.robin_farrow.dundlecraft.modules.resourceless;

import com.mojang.serialization.MapCodec;
import io.github.robin_farrow.dundlecraft.DundleCraft;
import io.github.robin_farrow.dundlecraft.core.DundleModule;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ResourceLessModule extends DundleModule
{
    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, DundleCraft.MODID);

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DundleCraft.MODID);
    public static final DeferredItem<Item> MONEL_IRON_MIX = ITEMS.registerSimpleItem("monel_iron_mix");

    @Override
    public void register(IEventBus modEventBus)
    {
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addItemsToCreativeTab);

        NeoForge.EVENT_BUS.register(new BabyGrowthEvents());
        NeoForge.EVENT_BUS.register(new CropGrowEvents());
        NeoForge.EVENT_BUS.register(new MobLootEvents());
        NeoForge.EVENT_BUS.register(new PlayerTillEvents());

        LOOT_MODIFIERS.register("damaged_equipment", () -> DamagedEquipmentLootModifier.CODEC);
        LOOT_MODIFIERS.register(modEventBus);
    }

    private void addItemsToCreativeTab(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(MONEL_IRON_MIX);
        }
    }
}
