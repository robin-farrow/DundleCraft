package io.github.robin_farrow.dundlecraft.modules.persistentblocks;

import io.github.robin_farrow.dundlecraft.mixin.persistentblocks.FallingBlockEntityInvoker;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PersistentFallingHandler
{
    private PersistentFallingHandler() {}

    private static final int SEARCH_RADIUS = 16;

    private static final Component OBSTRUCTED = Component.literal("Obstructed").withStyle(ChatFormatting.WHITE);
    private static final Component ABOVE_BUILD_LIMIT = Component.literal("Above build limit").withStyle(ChatFormatting.WHITE);
    // Where a thrown item starts (LivingEntity.createItemStackToDrop)
    public static Vec3 throwPosition(Player player) {
        return new Vec3(player.getX(), player.getEyeY() - 0.3F, player.getZ());
    }

    // Every offset within SEARCH_RADIUS, nearest first by straight-line distance
    private static final List<BlockPos> BLOCK_POS_SEARCH_ORDER = buildBlockPosSearchOrder();

    private static List<BlockPos> buildBlockPosSearchOrder() {
        List<BlockPos> offsets = new ArrayList<>();
        int limit = SEARCH_RADIUS * SEARCH_RADIUS;
        for (BlockPos offset : BlockPos.betweenClosed(-SEARCH_RADIUS, -SEARCH_RADIUS, -SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS))
            if (offset.distSqr(BlockPos.ZERO) <= limit)
                offsets.add(offset.immutable());
        offsets.sort(Comparator.comparingDouble(offset -> offset.distSqr(BlockPos.ZERO)));
        return List.copyOf(offsets);
    }

    // Set while a block is dropped as a plain item on purpose, so the join handler leaves it alone
    private static boolean keepingAsItem;

    public static boolean isKeepingAsItem() {
        return keepingAsItem;
    }

    // Whether a falling block starting here would have somewhere to go: a clear spot within half a block, or a free block space nearby
    public static boolean hasRoomToFall(ServerLevel level, Vec3 pos) {
        return unclip(level, pos, Vec3.ZERO) != null
                || nearestDestructible(level, BlockPos.containing(pos), null) != null;
    }

    // Destructible = no collision shape: air, torches, flowers, grass, rails, water...
    // Portals, powder snow and scaffolding count as solid: falling blocks land on top of them
    public static boolean isDestructible(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(BlockTags.PORTALS) || state.is(Blocks.POWDER_SNOW) || state.is(Blocks.SCAFFOLDING)) return false;
        return state.getCollisionShape(level, pos).isEmpty();
    }

    // The block a persistent item falls as, or null if it stays an item (not persistent, or carrying its own data)
    public static @Nullable BlockState fallingStateFor(ItemStack stack) {
        if (!PersistentBlocksRules.isPersistent(stack) || !stack.isComponentsPatchEmpty()) return null;
        return ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
    }

    // A player break: orientation survives the fall; everything else (lit, powered, open...) resets to default
    public static BlockState keepOrientation(BlockState fresh, BlockState broken) {
        if (fresh.getBlock() != broken.getBlock()) return fresh;
        for (Property<?> property : broken.getProperties())
            if (isOrientation(property))
                fresh = copyValue(fresh, broken, property);
        return fresh;
    }

    // Any other source: each orientation property gets a random value the block allows
    public static BlockState randomOrientation(BlockState state, RandomSource random) {
        for (Property<?> property : state.getProperties())
            if (isOrientation(property))
                state = randomValue(state, property, random);
        return state;
    }

    private static boolean isOrientation(Property<?> property) {
        Class<?> type = property.getValueClass();
        return type == Direction.class
                || type == Direction.Axis.class
                || type == FrontAndTop.class
                || property == BlockStateProperties.ROTATION_16
                || property == BlockStateProperties.HALF;
    }

    private static <T extends Comparable<T>> BlockState copyValue(BlockState to, BlockState from, Property<T> property) {
        return to.setValue(property, from.getValue(property));
    }

    private static <T extends Comparable<T>> BlockState randomValue(BlockState state, Property<T> property, RandomSource random) {
        List<T> values = property.getPossibleValues();
        return state.setValue(property, values.get(random.nextInt(values.size())));
    }

    public static boolean isDoubleSlab(BlockState state) {
        return state.hasProperty(SlabBlock.TYPE) && state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE;
    }

    // The same random throw vanilla gives dispensed items (DefaultDispenseItemBehavior.spawnItem, accuracy 6)
    public static Vec3 dispenserVelocity(RandomSource random, Direction facing) {
        double power = random.nextDouble() * 0.1 + 0.2;
        double spread = 0.0172275 * 6;
        return new Vec3(
                random.triangle(facing.getStepX() * power, spread),
                random.triangle(0.2, spread),
                random.triangle(facing.getStepZ() * power, spread));
    }

    // Spawns a falling block without touching the block already at that position
    public static void spawn(ServerLevel level, Vec3 pos, BlockState state, Vec3 velocity, @Nullable CompoundTag blockData) {
        FallingBlockEntity falling = FallingBlockEntityInvoker.dundlecraft$create(level, pos.x, pos.y, pos.z, state);
        falling.setDeltaMovement(velocity);
        falling.blockData = blockData;
        level.addFreshEntity(falling);
    }

    // Called instead of a falling block breaking into an item
    public static void snap(ServerLevel level, FallingBlockEntity broken) {
        BlockState state = broken.getBlockState();
        BlockPos target = nearestDestructible(level, broken.blockPosition(), broken);
        if (target == null) {
            dropAsItem(level, broken);
            return;
        }

        if (isDestructible(level, target.below())) {
            spawn(level, Vec3.atBottomCenterOf(target), state, Vec3.ZERO, broken.blockData);
            return;
        }

        if (!state.canSurvive(level, target) || !placeStationary(level, target, state, broken.blockData))
            dropAsItem(level, broken);
    }

    // A falling half slab landing on a bottom slab of the same kind joins it into a double slab
    public static boolean tryCombineSlab(ServerLevel level, FallingBlockEntity falling) {
        BlockState state = falling.getBlockState();
        if (!(state.getBlock() instanceof SlabBlock) || isDoubleSlab(state)) return false;

        BlockPos pos = falling.blockPosition();
        BlockState existing = level.getBlockState(pos);
        if (!existing.is(state.getBlock()) || existing.getValue(SlabBlock.TYPE) != SlabType.BOTTOM) return false;

        BlockState combined = existing.setValue(SlabBlock.TYPE, SlabType.DOUBLE).setValue(SlabBlock.WATERLOGGED, false);
        if (!level.setBlockAndUpdate(pos, combined)) return false;
        playPlaceSound(level, pos, combined);
        return true;
    }

    // Nearest destructible block space with no other falling block in it, or null if there's none nearby
    public static @Nullable BlockPos nearestDestructible(ServerLevel level, BlockPos origin, @Nullable FallingBlockEntity self) {
        for (BlockPos offset : BLOCK_POS_SEARCH_ORDER) {
            BlockPos pos = origin.offset(offset);
            if (isDestructible(level, pos) && !hasFallingBlock(level, pos, self))
                return pos;
        }
        return null;
    }

    // Breaks whatever destructible block is there, then places the block in its spot
    private static boolean placeStationary(ServerLevel level, BlockPos pos, BlockState state, @Nullable CompoundTag blockData) {
        level.destroyBlock(pos, true);
        if (state.hasProperty(BlockStateProperties.WATERLOGGED) && level.getFluidState(pos).is(Fluids.WATER))
            state = state.setValue(BlockStateProperties.WATERLOGGED, true);
        if (!level.setBlockAndUpdate(pos, state)) return false;

        playPlaceSound(level, pos, state);
        if (blockData != null && level.getBlockEntity(pos) instanceof BlockEntity blockEntity) {
            blockEntity.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), blockData));
            blockEntity.setChanged();
        }
        return true;
    }

    // Same sound and volume as placing the block by hand (BlockItem.place)
    public static void playPlaceSound(Level level, BlockPos pos, BlockState state) {
        SoundType sound = state.getSoundType(level, pos, null);
        level.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
    }

    // Last resort, when there's nowhere valid nearby: a normal item that the join handler won't convert back
    private static void dropAsItem(ServerLevel level, FallingBlockEntity broken) {
        BlockState state = broken.getBlockState();
        keepingAsItem = true;
        try {
            broken.spawnAtLocation(level, new ItemStack(state.getBlock(), isDoubleSlab(state) ? 2 : 1));
        } finally {
            keepingAsItem = false;
        }
    }

    // Another falling block is already in this block space
    // Another falling block is already in this block space
    private static boolean hasFallingBlock(ServerLevel level, BlockPos pos, @Nullable FallingBlockEntity self) {
        return !level.getEntitiesOfClass(FallingBlockEntity.class, new AABB(pos), entity -> entity != self && entity.isAlive()).isEmpty();
    }

    private static final double MAX_UNCLIP = 0.5;
    private static final double UNCLIP_MARGIN = 1.0E-4;

    // Axis combinations to try, as indexes into the axes sorted slowest first
    private static final int[][] AXIS_SUBSETS = {{0}, {1}, {2}, {0, 1}, {0, 2}, {1, 2}, {0, 1, 2}};

    public static void warnObstructed(Player player) {
        player.sendOverlayMessage(OBSTRUCTED);
    }

    // A persistent block can't be thrown if there's nowhere within half a block for it to spawn unclipped
    // Why this player can't throw this persistent block right now, or null if they can
    public static @Nullable Component throwProblem(Player player, ItemStack stack) {
        if (fallingStateFor(stack) == null) return null;
        if (Mth.floor(player.getEyeY()) > player.level().getMaxY()) return ABOVE_BUILD_LIMIT;
        if (unclip(player.level(), throwPosition(player), Vec3.ZERO) == null) return OBSTRUCTED;
        return null;
    }

    // Moves a falling block the least distance (at most half a block per axis) out of any blocks it would spawn inside.
    // Axes it moves slowest along are tried first, so it keeps heading the way it was thrown. Null if there's no room.
    public static @Nullable Vec3 unclip(Level level, Vec3 pos, Vec3 velocity) {
        AABB box = EntityTypes.FALLING_BLOCK.getDimensions().makeBoundingBox(pos);
        if (level.noBlockCollision(null, box)) return pos;

        List<AABB> obstacles = new ArrayList<>();
        for (VoxelShape shape : level.getBlockCollisions(null, box.inflate(MAX_UNCLIP)))
            obstacles.addAll(shape.toAabbs());

        List<Direction.Axis> axes = new ArrayList<>(List.of(Direction.Axis.values()));
        axes.sort(Comparator.comparingDouble(axis -> Math.abs(velocity.get(axis))));

        for (int[] subset : AXIS_SUBSETS) {
            Vec3 offset = smallestFreeOffset(level, box, obstacles, axes, subset, 0, Vec3.ZERO);
            if (offset != null) return pos.add(offset);
        }
        return null;
    }

    // Tries every combination of clearing shifts along the chosen axes, keeping the smallest that's free
    private static @Nullable Vec3 smallestFreeOffset(Level level, AABB box, List<AABB> obstacles,
                                                     List<Direction.Axis> axes, int[] subset, int index, Vec3 offset) {
        if (index == subset.length)
            return level.noBlockCollision(null, box.move(offset)) ? offset : null;

        Direction.Axis axis = axes.get(subset[index]);
        Vec3 best = null;
        for (double shift : clearingShifts(axis, box, obstacles)) {
            Vec3 found = smallestFreeOffset(level, box, obstacles, axes, subset, index + 1, offset.with(axis, shift));
            if (found != null && (best == null || found.lengthSqr() < best.lengthSqr()))
                best = found;
        }
        return best;
    }

    // Distances along one axis that would clear each obstacle, either way, up to half a block
    private static List<Double> clearingShifts(Direction.Axis axis, AABB box, List<AABB> obstacles) {
        List<Double> shifts = new ArrayList<>();
        for (AABB obstacle : obstacles) {
            double positive = obstacle.max(axis) - box.min(axis) + UNCLIP_MARGIN;
            double negative = obstacle.min(axis) - box.max(axis) - UNCLIP_MARGIN;
            if (positive > 0 && positive <= MAX_UNCLIP) shifts.add(positive);
            if (negative < 0 && -negative <= MAX_UNCLIP) shifts.add(negative);
        }
        return shifts;
    }
}