package io.github.robin_farrow.dundlecraft.modules.resourceless;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.Optional;

public class DamagedEquipmentLootModifier extends LootModifier
{
    public static final MapCodec<DamagedEquipmentLootModifier> CODEC =
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, DamagedEquipmentLootModifier::new));

    public DamagedEquipmentLootModifier(Optional<Holder<LootItemCondition>> condition, int priority) {
        super(condition, priority);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!context.getQueriedLootTableId().getPath().startsWith("chests/")) {
            return generatedLoot;
        }
        RandomSource random = context.getRandom();
        for (ItemStack stack : generatedLoot) {
            if (stack.isDamageableItem()) {
                int max = stack.getMaxDamage();
                // Same wear formula vanilla uses for mob-dropped gear
                stack.setDamageValue(max - random.nextInt(1 + random.nextInt(Math.max(max - 3, 1))));
            }
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
