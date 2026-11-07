package io.github.robin_farrow.dundlecraft.modules.dundlehardcore;

import io.github.robin_farrow.dundlecraft.DundleCraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class PlayerAttributeEvents
{
    private static final Identifier BREAK_SPEED_ID = Identifier.fromNamespaceAndPath(DundleCraft.MODID, "slow_mining");
    private static final Identifier ATTACK_SPEED_ID = Identifier.fromNamespaceAndPath(DundleCraft.MODID, "slow_attack");

    private static final double BREAK_SPEED_MULTIPLIER = 0.25;
    private static final double ATTACK_SPEED_MULTIPLIER = 0.60;

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        applyDebuffs(event.getEntity());
    }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        applyDebuffs(event.getEntity());
    }

    private static void applyDebuffs(Player player)
    {
        apply(player, Attributes.BLOCK_BREAK_SPEED, BREAK_SPEED_ID, BREAK_SPEED_MULTIPLIER - 1);
        apply(player, Attributes.ATTACK_SPEED, ATTACK_SPEED_ID, ATTACK_SPEED_MULTIPLIER - 1);
    }

    private static void apply(Player player, Holder<Attribute> attribute, Identifier id, double amount)
    {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.addOrUpdateTransientModifier(
                    new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }
}
