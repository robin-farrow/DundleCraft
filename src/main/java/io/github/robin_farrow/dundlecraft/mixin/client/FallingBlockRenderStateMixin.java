package io.github.robin_farrow.dundlecraft.mixin.client;

import io.github.robin_farrow.dundlecraft.client.FallingBlockItemRender;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FallingBlockRenderState.class)
public abstract class FallingBlockRenderStateMixin implements FallingBlockItemRender
{
    @Unique
    private final ItemStackRenderState dundlecraft$item = new ItemStackRenderState();

    @Override
    public ItemStackRenderState dundlecraft$item() {
        return dundlecraft$item;
    }
}
