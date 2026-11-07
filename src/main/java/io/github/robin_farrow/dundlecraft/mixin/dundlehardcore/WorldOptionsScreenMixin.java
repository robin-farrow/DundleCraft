package io.github.robin_farrow.dundlecraft.mixin.dundlehardcore;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.WorldOptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldOptionsScreen.class)
public abstract class WorldOptionsScreenMixin
{
    private static final Tooltip LOCKED_TOOLTIP = Tooltip.create(Component.translatable("dundlecraft.dundlehardcore.commands_disabled"));

    // Allow Commands can only be chosen when creating the world, not changed afterwards
    @ModifyReturnValue(method = "createAllowCommandsButton", at = @At("RETURN"))
    private CycleButton<Boolean> dundlecraft$lockAllowCommands(CycleButton<Boolean> button) {
        button.active = false;
        button.setTooltip(LOCKED_TOOLTIP);
        return button;
    }
}
