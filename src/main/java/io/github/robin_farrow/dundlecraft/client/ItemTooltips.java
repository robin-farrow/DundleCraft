package io.github.robin_farrow.dundlecraft.client;

import io.github.robin_farrow.dundlecraft.modules.persistentblocks.PersistentBlocksRules;
import io.github.robin_farrow.dundlecraft.modules.playersoul.PlayerSoulModule;
import io.github.robin_farrow.dundlecraft.modules.truedark.lightburnout.LightBurnout;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.StringJoiner;
import java.util.function.Consumer;

public final class ItemTooltips
{
    private static final int PERSISTENT_COLOUR = 0x3C84E8;
    private static final int BURNOUT_COLOUR = 0xDE9E34;

    // "Persistent", with its rules listed while Shift is held
    public static void persistent(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                  @Nullable Player player, TooltipFlag flag, Consumer<Component> builder)
    {
        if (!PersistentBlocksRules.isPersistent(stack)) return;

        builder.accept(Component.literal("Persistent").withStyle(style -> style.withColor(PERSISTENT_COLOUR)));

        if (Minecraft.getInstance().hasShiftDown()) {
            builder.accept(Component.literal("- Drops as a falling block").withStyle(style -> style.withColor(PERSISTENT_COLOUR)));
            builder.accept(Component.literal("- Falls out of containers").withStyle(style -> style.withColor(PERSISTENT_COLOUR)));
            builder.accept(Component.literal("  - 1 Block falls out every " + formatDuration(PersistentBlocksRules.HEAVY_CARGO_DROP_TICKS)).withStyle(style -> style.withColor(PERSISTENT_COLOUR)));
            builder.accept(Component.literal("  - Interact with container to reset timer").withStyle(style -> style.withColor(PERSISTENT_COLOUR)));
            builder.accept(Component.literal("  - Cannot be stored in some containers").withStyle(style -> style.withColor(PERSISTENT_COLOUR)));
        } else {
            builder.accept(Component.literal(" Hold ").withStyle(style -> style.withColor(ChatFormatting.DARK_GRAY))
                    .append(Component.literal("[Shift]").withStyle(style -> style.withColor(ChatFormatting.GRAY)))
                    .append(Component.literal(" for details").withStyle(style -> style.withColor(ChatFormatting.DARK_GRAY))));
        }
    }

    // How long light sources burn before going out
    public static void burnout(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                               @Nullable Player player, TooltipFlag flag, Consumer<Component> builder)
    {
        if (!(stack.getItem() instanceof BlockItem blockItem)) return;
        BlockState state = blockItem.getBlock().defaultBlockState();
        if (!state.hasProperty(LightBurnout.BURN)) return;

        builder.accept(Component.literal("Burnout: " + formatDuration(LightBurnout.burnLifetimeTicks(state))).withStyle(style -> style.withColor(BURNOUT_COLOUR)));
    }

    // Soul items, and the totem that uses them
    public static void soul(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                            @Nullable Player player, TooltipFlag flag, Consumer<Component> builder)
    {
        int restored = PlayerSoulModule.soulRestoredBy(stack);
        if (restored > 0)
            builder.accept(Component.literal("Restores " + restored + " Soul upon Undying").withStyle(ChatFormatting.AQUA));
        else if (stack.is(Items.TOTEM_OF_UNDYING))
        {
            builder.accept(Component.literal("Restores Soul when used").withStyle(ChatFormatting.AQUA));
            builder.accept(Component.literal("with an Ensouled item").withStyle(ChatFormatting.AQUA));
        }
    }

    // "1h 2m 3s", "2h 3m", "5h", "2m 5s", "45s": zero units are left out
    private static String formatDuration(int ticks)
    {
        int totalSeconds = ticks / 20;
        int hours = totalSeconds / 3600;
        int minutes = totalSeconds / 60 % 60;
        int seconds = totalSeconds % 60;

        StringJoiner time = new StringJoiner(" ");
        if (hours > 0) time.add(hours + "h");
        if (minutes > 0) time.add(minutes + "m");
        if (seconds > 0) time.add(seconds + "s");
        return time.length() > 0 ? time.toString() : "0s";
    }
}
