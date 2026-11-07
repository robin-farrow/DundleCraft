package io.github.robin_farrow.dundlecraft.modules.resourceless;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;

import java.util.List;
import java.util.stream.IntStream;

public final class StructureGenerationRules
{
    private StructureGenerationRules() {}

    private static ProcessorRule replace(Block from, Block to)
    {
        return new ProcessorRule(new BlockMatchTest(from), AlwaysTrueTest.INSTANCE, to.defaultBlockState());
    }

    // Don't replace with AIR, as that will actually allow the original terrain to generate in that block
    public static final StructureProcessor VILLAGE_REPLACE_BLOCKS = new RuleProcessor(List.of(
//            replace(Blocks.LECTERN, Blocks.BOOKSHELF),
//            replace(Blocks.COMPOSTER, Blocks.AIR),
//            replace(Blocks.BARREL, Blocks.OAK_PLANKS),
            replace(Blocks.SMOKER, Blocks.CRAFTING_TABLE),
            replace(Blocks.BLAST_FURNACE, Blocks.COPPER_GRATE.waxed().unaffected()),
//            replace(Blocks.CARTOGRAPHY_TABLE, Blocks.CRAFTING_TABLE),
//            replace(Blocks.FLETCHING_TABLE, Blocks.CRAFTING_TABLE),
            replace(Blocks.GRINDSTONE, Blocks.CRAFTING_TABLE),
//            replace(Blocks.SMITHING_TABLE, Blocks.SMOOTH_STONE),
            replace(Blocks.STONECUTTER, Blocks.CRAFTING_TABLE),
//            replace(Blocks.LOOM, Blocks.WOOL.white()),
            replace(Blocks.BREWING_STAND, Blocks.CRAFTING_TABLE),
            replace(Blocks.CAULDRON, Blocks.CRAFTING_TABLE),
            replace(Blocks.WATER_CAULDRON, Blocks.CRAFTING_TABLE),
            replace(Blocks.LAVA_CAULDRON, Blocks.CRAFTING_TABLE),
            replace(Blocks.POWDER_SNOW_CAULDRON, Blocks.CRAFTING_TABLE)

//            replace(Blocks.HAY_BLOCK, Blocks.OAK_WOOD),
//            replace(Blocks.PUMPKIN, Blocks.SPRUCE_WOOD),
//            replace(Blocks.MELON, Blocks.ACACIA_WOOD)
    ));

    public static final StructureProcessor TRIAL_REPLACE_COPPER = new RuleProcessor(List.of(
            replace(Blocks.COPPER_BLOCK.weathering().unaffected(), Blocks.CUT_COPPER.weathering().unaffected()),
            replace(Blocks.COPPER_BLOCK.waxed().unaffected(), Blocks.CUT_COPPER.waxed().unaffected())
    ));
}
