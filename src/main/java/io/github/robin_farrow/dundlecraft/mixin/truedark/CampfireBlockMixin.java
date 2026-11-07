package io.github.robin_farrow.dundlecraft.mixin.truedark;

import io.github.robin_farrow.dundlecraft.modules.truedark.lightburnout.LightBurnout;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin extends BaseEntityBlock {
    protected CampfireBlockMixin(Properties properties) {
        super(properties);
    }

    // CampfireBlock declares its own state list, so inject into it
    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void dundlecraft$addBurn(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(LightBurnout.BURN);
    }

    // Vanilla campfires don't random tick, so switch it on
    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(CampfireBlock.LIT)) {
            return; // only lit campfires age
        }
        LightBurnout.tick(state, level, pos, random,
                () -> level.destroyBlock(pos, true));
    }
}