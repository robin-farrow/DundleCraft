package io.github.robin_farrow.dundlecraft.modules.truedark.lightburnout;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class LightBurnout
{
    public static final int MAX_BURN = 5; // 0 = fresh, MAX_BURN = one random tick before burnout
    public static final IntegerProperty BURN = IntegerProperty.create("burn", 0, MAX_BURN);

    private final static int TICKS_PER_MINUTE = 60 * 20;
    // Average game ticks between random ticks for one block at the default randomTickSpeed of 3
    private static final double AVG_RANDOM_TICK_INTERVAL = 4096d / 3d;

    private final static int CAMPFIRE_TICKS = 15 * TICKS_PER_MINUTE;
    private final static int TORCH_TICKS = 30 * TICKS_PER_MINUTE;
    private final static int LONG_TORCH_TICKS = 120 * TICKS_PER_MINUTE;

    // Chance of an extra puff per animateTick call increasing from 0 up to this value at max burn
    private static final float MAX_FLICKER_CHANCE = 0.6f;

    private LightBurnout() {}

    /** Shared random tick logic: maybe advance the burn stage, and run onBurnout after the last one. */
    public static void tick(BlockState state, ServerLevel level, BlockPos pos,
                            RandomSource random, Runnable onBurnout) {
        int expectedRandomTicks = Math.max(1,
                (int) (burnLifetimeTicks(state) / AVG_RANDOM_TICK_INTERVAL));

        int events = MAX_BURN + 1; // stage changes plus the final burnout
        if (random.nextInt(expectedRandomTicks) >= events) {
            return; // no advance this tick (chance = events / expectedRandomTicks)
        }

        int burn = state.getValue(BURN);
        if (burn >= MAX_BURN) {
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.05F, 1.0F);
            level.sendParticles(ParticleTypes.SMOKE,
                    pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 8, 0.1, 0.1, 0.1, 0.02);
            onBurnout.run();
        } else {
            level.setBlock(pos, state.setValue(BURN, burn + 1), Block.UPDATE_CLIENTS);
        }
    }

    /** Light level for a lit block at the given burn stage: 100% when fresh, 50% at the last stage. */
    public static int dimmedLight(int baseLight, int burn) {
        if (baseLight <= 0) {
            return baseLight; // unlit campfires must stay at 0
        }
        float remaining = 1F - 0.5F * burn / MAX_BURN;
        return Math.max(1, Math.round(baseLight * remaining));
    }

    /** How long this block should last, in game ticks. */
    public static int burnLifetimeTicks(BlockState state) {
        boolean isCampfire = state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE);
        boolean isLongTorch = state.is(Blocks.SOUL_TORCH) || state.is(Blocks.SOUL_WALL_TORCH)
                || state.is(Blocks.COPPER_TORCH) || state.is(Blocks.COPPER_WALL_TORCH);
        return  isCampfire ? CAMPFIRE_TICKS
                : isLongTorch ? LONG_TORCH_TICKS
                : TORCH_TICKS;
    }
}