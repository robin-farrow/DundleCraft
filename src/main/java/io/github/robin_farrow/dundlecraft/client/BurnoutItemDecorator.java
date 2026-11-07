package io.github.robin_farrow.dundlecraft.client;

import io.github.robin_farrow.dundlecraft.DundleCraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;

public final class BurnoutItemDecorator implements IItemDecorator
{
    private static final Identifier SPRITE = Identifier.fromNamespaceAndPath(DundleCraft.MODID, "hud/burnout");

    // A small flame in the top-left corner of light sources that burn out
    @Override
    public boolean render(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE, x, y, 3, 3);
        return false;
    }
}
