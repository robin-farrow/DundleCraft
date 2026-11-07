package io.github.robin_farrow.dundlecraft.modules.dundlehardcore;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.ServerChatEvent;

import java.util.Locale;
import java.util.Set;

public final class CancelChatEvents
{
    private static final Set<String> CHAT_COMMANDS = Set.of("msg", "tell", "w", "me", "teammsg", "tm");

    @SubscribeEvent
    public void onChat(ServerChatEvent event)
    {
        event.setCanceled(true);

        event.getPlayer().sendOverlayMessage(Component.translatable("dundlecraft.dundlehardcore.chat_disabled"));
    }

    @SubscribeEvent
    public void onCommand(CommandEvent event)
    {
        CommandSourceStack source = event.getParseResults().getContext().getSource();
        if (source.getPlayer() == null) return; // console and command blocks can still use them

        String input = event.getParseResults().getReader().getString().trim();
        if (input.startsWith("/")) input = input.substring(1);
        String command = input.split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        // Strip the command namespace
        command = command.substring(command.indexOf(':') + 1);

        if (CHAT_COMMANDS.contains(command)) {
            event.setCanceled(true);
            source.getPlayer().sendOverlayMessage(Component.translatable("dundlecraft.dundlehardcore.chat_disabled"));
        }
    }
}
