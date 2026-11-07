package io.github.robin_farrow.dundlecraft.modules.signalflare;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;

public final class SignalFlareItem extends Item
{
    private static final int FLIGHT_DURATION = 3;  // like a Firework Rocket: higher = flies higher
    private static final String[] DIRECTIONS =
            {"North", "Northeast", "East", "Southeast", "South", "Southwest", "West", "Northwest"};

    public SignalFlareItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ItemStack stack = context.getItemInHand();
        FireworkExplosion explosion = stack.get(DataComponents.FIREWORK_EXPLOSION);
        if (explosion == null) return InteractionResult.PASS;  // an uncoloured flare does nothing

        Level level = context.getLevel();
        Vec3 click = context.getClickLocation();
        Direction face = context.getClickedFace();
        double x = click.x + face.getStepX() * 0.15;  // same offset vanilla uses
        double y = click.y + face.getStepY() * 0.15;
        double z = click.z + face.getStepZ() * 0.15;

        if (y < level.getSeaLevel()) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.literal("You can't fire a Signal Flare below sea level.")
                        .withStyle(ChatFormatting.RED));
            }
            return InteractionResult.FAIL;
        }

        if (level instanceof ServerLevel serverLevel) {
            // The flying entity carries a copy of the flare itself, plus the FIREWORKS data it explodes with
            ItemStack rocketStack = stack.copyWithCount(1);
            rocketStack.set(DataComponents.FIREWORKS, new Fireworks(FLIGHT_DURATION, List.of(explosion)));
            Projectile.spawnProjectile(new FireworkRocketEntity(level, player, x, y, z, rocketStack), serverLevel, rocketStack);

            announce(serverLevel, player, stack.getItemName(), explosion, x, z);
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    private static void announce(ServerLevel level, Player launcher, Component flareName,
                                 FireworkExplosion explosion, double x, double z)
    {
        int colour = brighten(explosion.colors().isEmpty() ? 0xFFFFFF : explosion.colors().getInt(0));

        String text = flareName.getString();
        String article = "AEIOU".indexOf(text.charAt(0)) >= 0 ? "an" : "a";
        launcher.sendSystemMessage(Component.literal("Fired " + article + " ").append(flareName).append(".")
                .withStyle(style -> style.withColor(colour)));

        for (ServerPlayer other : level.players()) {
            if (other == launcher) continue;
            String where = describe(x - other.getX(), z - other.getZ());       // from the other player to the flare
            other.sendSystemMessage(Component.empty().append(flareName).append(" was fired " + where)
                    .withStyle(style -> style.withColor(colour)));
        }
    }

    // Mix 35% white into the colour so dark dyes are still readable
    public static int brighten(int rgb)
    {
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        r += (255 - r) * 35 / 100;
        g += (255 - g) * 35 / 100;
        b += (255 - b) * 35 / 100;
        return (r << 16) | (g << 8) | b;
    }

    private static String describe(double dx, double dz)
    {
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance < 50) return "nearby";

        double rounded;
        if (distance < 100) {
            rounded = Math.round(distance / 50.0) * 50.0;                           // 50m to 100m: nearest 50
        } else {
            double step = Math.pow(10, Math.floor(Math.log10(distance)));            // 100s, 1000s, 10000s...
            rounded = Math.round(distance / step) * step;
        }

        double bearing = Math.toDegrees(Math.atan2(dx, -dz));                        // 0 = north (-Z), 90 = east (+X)
        String direction = DIRECTIONS[Math.floorMod((int) Math.round(bearing / 45.0), 8)];

        String text = rounded >= 1000 ? (long) (rounded / 1000) + "km" : (long) rounded + "m";
        return text + " " + direction;
    }

    // Description shown under the flare's name
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag)
    {
        builder.accept(Component.literal("Broadcast your location to").withStyle(ChatFormatting.DARK_RED));
        builder.accept(Component.literal("all players in this dimension").withStyle(ChatFormatting.DARK_RED));

    }
}