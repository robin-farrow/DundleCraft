package io.github.robin_farrow.dundlecraft.modules.playersoul;

import io.github.robin_farrow.dundlecraft.DundleCraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public final class SoulClientHud
{
    public static final Identifier LAYER_ID = Identifier.fromNamespaceAndPath(DundleCraft.MODID, "soul");

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker)
    {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.gui.hud.isHidden() || !minecraft.gameMode.canHurtPlayer()) return;

        Hud hud = minecraft.gui.hud;
        int lives = player.getData(PlayerSoulModule.SOUL);

        int width = (PlayerSoulModule.MAX_SOUL - 1) * 8 + 9;     // 3 hearts, 8px apart, last one 9px wide
        int gap = 4;
        int xLeft = graphics.guiWidth() / 2 - 91 - gap - width;
        int y = graphics.guiHeight() - hud.leftHeight;       // same row as the red hearts

        for (int i = 0; i < PlayerSoulModule.MAX_SOUL; i++) {
            int x = xLeft + i * 8;
            Identifier sprite = i < lives ? PlayerSoulModule.SOUL_FULL : PlayerSoulModule.SOUL_EMPTY;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 9, 9);
        }
    }
}
