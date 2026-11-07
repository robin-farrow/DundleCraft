package io.github.robin_farrow.dundlecraft.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.robin_farrow.dundlecraft.client.FallingBlockItemRender;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockRenderer.class)
public abstract class FallingBlockRendererMixin
{
    @Unique
    private ItemModelResolver dundlecraft$itemModels;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void dundlecraft$keepItemModels(EntityRendererProvider.Context context, CallbackInfo ci) {
        dundlecraft$itemModels = context.getItemModelResolver();
    }

    // Blocks with no block model get their item model instead
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/item/FallingBlockEntity;Lnet/minecraft/client/renderer/entity/state/FallingBlockRenderState;F)V",
            at = @At("TAIL"))
    private void dundlecraft$extractItem(FallingBlockEntity entity, FallingBlockRenderState state, float partialTicks, CallbackInfo ci) {
        ItemStackRenderState item = ((FallingBlockItemRender) state).dundlecraft$item();
        BlockState blockState = entity.getBlockState();
        if (FallingBlockItemRender.drawnAsItem(blockState))
            dundlecraft$itemModels.updateForNonLiving(item, new ItemStack(blockState.getBlock()), ItemDisplayContext.NONE, entity);
        else
            item.clear();
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/FallingBlockRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("TAIL"))
    private void dundlecraft$submitItem(FallingBlockRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        ItemStackRenderState item = ((FallingBlockItemRender) state).dundlecraft$item();
        if (item.isEmpty()) return;

        BlockState blockState = state.movingBlockRenderState.blockState;
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.5F, 0.0F);   // item models are centred; a falling block sits on its bottom face
        if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
            poseStack.rotateDegrees(Axis.YP, -blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot());
        item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
