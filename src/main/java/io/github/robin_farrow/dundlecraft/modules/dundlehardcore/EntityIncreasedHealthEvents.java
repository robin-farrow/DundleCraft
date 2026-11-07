package io.github.robin_farrow.dundlecraft.modules.dundlehardcore;

import io.github.robin_farrow.dundlecraft.DundleCraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Set;

public class EntityIncreasedHealthEvents
{
    private static final Identifier INCREASED_HEALTH_ID = Identifier.fromNamespaceAndPath(DundleCraft.MODID, "double_health");
    private static final int REGEN_INTERVAL = 15 * 20;  // Every 15 seconds
    private static final int REGEN = 1;
    private static final float HEAL_MULTIPLIER = 2f;
    private static final float MAX_HEALTH_MULTIPLIER = 2f;

    private static final Set<EntityType<?>> EXEMPT_TYPES = Set.of(
            EntityTypes.IRON_GOLEM,
            EntityTypes.SNOW_GOLEM,
            EntityTypes.PLAYER,
            EntityTypes.STRIDER,
            EntityTypes.HAPPY_GHAST,
            EntityTypes.ALLAY,
            EntityTypes.VILLAGER,
            EntityTypes.ARMOR_STAND
    );

    private static boolean isExempt(LivingEntity entity)
    {
        return EXEMPT_TYPES.contains(entity.getType())
                // Pets: Wolves, Cats, Parrots, Nautilus, ZombieNautilus
                || (entity instanceof TamableAnimal pet)
                // Horse, Donkey, Mule, Llama, TraderLlama, SkeletonHorse, ZombieHorse, Camel, CamelHusk
                || (entity instanceof AbstractHorse horse);
    }

    @SubscribeEvent
    public void onJoin(EntityJoinLevelEvent event)
    {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof LivingEntity living) || isExempt(living)) return;

        AttributeInstance maxHealth = living.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null || maxHealth.hasModifier(INCREASED_HEALTH_ID)) return;

        float before = living.getHealth();
        maxHealth.addOrReplacePermanentModifier(
                new AttributeModifier(INCREASED_HEALTH_ID, MAX_HEALTH_MULTIPLIER - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        living.setHealth(before * MAX_HEALTH_MULTIPLIER);
    }

    @SubscribeEvent
    public void onHeal(LivingHealEvent event)
    {
        AttributeInstance maxHealth = event.getEntity().getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null && maxHealth.hasModifier(INCREASED_HEALTH_ID)) {
            event.setAmount(event.getAmount() * HEAL_MULTIPLIER);
        }
    }

    // Mobs with doubled health slowly recover over time
    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Post event)
    {
        if (!(event.getEntity() instanceof LivingEntity living) || living.tickCount % REGEN_INTERVAL != 0) return;
        if (living.level().isClientSide() || !living.isAlive() || living.getHealth() >= living.getMaxHealth()) return;

        AttributeInstance maxHealth = living.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null || !maxHealth.hasModifier(INCREASED_HEALTH_ID)) return;

        // Divide by heal multiplier, because will be multiplied by it in onHeal
        living.heal(REGEN / HEAL_MULTIPLIER);
    }
}
