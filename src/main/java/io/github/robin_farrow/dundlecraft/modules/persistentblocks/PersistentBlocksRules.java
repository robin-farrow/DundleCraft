package io.github.robin_farrow.dundlecraft.modules.persistentblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PersistentBlocksRules
{
    public static final int HEAVY_CARGO_DROP_TICKS = 10 * 20;  // 30 minutes

    // Solid blocks that aren't heavy
    private static Set<Block> getHeavyBlockExceptions() {
        return Stream.of(
                // Single blocks
//                Stream.of(
//                ),
                // Block classes
                blocksOfType(LanternBlock.class),
                blocksOfType(ChainBlock.class),
                blocksOfType(PressurePlateBlock.class),
                blocksOfType(RodBlock.class),
                blocksOfType(AbstractCandleBlock.class),
                blocksOfType(CarpetBlock.class),
                blocksOfType(FlowerPotBlock.class),
                blocksOfType(AbstractSkullBlock.class),
                blocksOfType(AmethystClusterBlock.class),
                blocksOfType(SeaPickleBlock.class),
                blocksOfType(LadderBlock.class),
                blocksOfType(LilyPadBlock.class),
                blocksOfType(CakeBlock.class),
                blocksOfType(DiodeBlock.class),
                blocksOfType(ChorusFlowerBlock.class),
                blocksOfType(ChorusPlantBlock.class),
                blocksOfType(CactusBlock.class),
                blocksOfType(BambooStalkBlock.class),
                blocksOfType(CocoaBlock.class),
                blocksOfType(BigDripleafBlock.class),
                blocksOfType(AzaleaBlock.class),
                blocksOfType(TurtleEggBlock.class),
                blocksOfType(SnifferEggBlock.class),
                blocksOfType(PointedDripstoneBlock.class),
                blocksOfType(SulfurSpikeBlock.class)
        ).flatMap(s -> s).collect(Collectors.toUnmodifiableSet());
    }

    // Heavy blocks that aren't persistent
    private static Set<Block> getPersistentBlockExceptions() {
        return Stream.of(
                // Single blocks
//                Stream.of(
//                ),
                // Block classes
                blocksOfType(CampfireBlock.class),
                blocksOfType(DoorBlock.class), // Halved
                blocksOfType(AbstractBedBlock.class), // Halved
//                blocksOfType(AbstractChestBlock.class), // Invisible
                blocksOfType(BannerBlock.class), // Invisible
                blocksOfType(TrapDoorBlock.class),
                blocksOfType(BellBlock.class),
                blocksOfType(ConduitBlock.class)
//                blocksOfType(DecoratedPotBlock.class), // Invisible
//                blocksOfType(ShulkerBoxBlock.class) // Invisible
        ).flatMap(s -> s).collect(Collectors.toUnmodifiableSet());
    }

    // Containers that refuse persistent blocks
    public static boolean cantStorePersistent(Container container) {
        return container instanceof ChestBlockEntity  // chests, trapped chests, copper chests
                || container instanceof CompoundContainer  // double chests
                || container instanceof BarrelBlockEntity
                || container instanceof PlayerEnderChestContainer
                || container instanceof ShelfBlockEntity;
                // Shulker Boxes and bundles covered by mixin

        // || container instanceof MinecartChest
        // || container instanceof AbstractChestBoat
    }

    private static Set<Block> heavyBlocks = Set.of();
    private static Set<Block> persistentBlockExceptions = Set.of();

    private PersistentBlocksRules() {}

    public static void init()
    {
        Set<Block> heavyBlockExceptions = getHeavyBlockExceptions();
        Set<Block> blocks = new HashSet<>();
        for (Block block : BuiltInRegistries.BLOCK)
            if (isSolid(block) && !heavyBlockExceptions.contains(block))
                blocks.add(block);
        heavyBlocks = Set.copyOf(blocks);
        persistentBlockExceptions = getPersistentBlockExceptions();
    }

    // Every registered block of the given type, including subclasses and modded blocks
    private static Stream<Block> blocksOfType(Class<? extends Block> type) {
        return BuiltInRegistries.BLOCK.stream().filter(type::isInstance);
    }

    // Solid = has any collision shape, so stairs, slabs, fences and walls all count
    private static boolean isSolid(Block block) {
        return !block.defaultBlockState().getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty();
    }

    // Most solid blocks
    public static boolean isHeavy(Block block) {
        return heavyBlocks.contains(block);
    }

    public static boolean isHeavy(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem && isHeavy(blockItem.getBlock());
    }

    // Most Heavy Blocks are Persistent, with some exceptions
    public static boolean isPersistent(Block block) {
        return isHeavy(block) && !persistentBlockExceptions.contains(block);
    }

    public static boolean isPersistent(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem && isPersistent(blockItem.getBlock());
    }
}
