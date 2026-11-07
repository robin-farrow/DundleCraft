package io.github.robin_farrow.dundlecraft.mixin.truedark;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.robin_farrow.dundlecraft.modules.truedark.lightburnout.LightBurnout;
import java.util.function.ToIntFunction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    // Runs once per block state at startup, not every time light is read
    @WrapOperation(
            method = "<init>",
            at = @At(value = "INVOKE",
                    target = "Ljava/util/function/ToIntFunction;applyAsInt(Ljava/lang/Object;)I",
                    ordinal = 0))
    private int dundlecraft$dimBurningLight(ToIntFunction<?> lightEmission, Object stateObject,
                                            Operation<Integer> original) {
        int baseLight = original.call(lightEmission, stateObject);
        BlockState state = (BlockState) stateObject;
        return state.hasProperty(LightBurnout.BURN)
                ? LightBurnout.dimmedLight(baseLight, state.getValue(LightBurnout.BURN))
                : baseLight;
    }
}