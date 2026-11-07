package io.github.robin_farrow.dundlecraft.modules.resourceless;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

public class PlayerTillEvents
{
    @SubscribeEvent
    public void onTill(PlayerInteractEvent.RightClickBlock event)
    {
        Player player = event.getEntity();
        if (!event.getItemStack().is(ItemTags.HOES) || player.hasInfiniteMaterials()) return;

        // Same conditions as the vanilla hoe transformer for making farmland
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (event.getFace() == Direction.DOWN) return;
        if (!level.getBlockState(pos).is(BlockTags.TURNS_INTO_FARMLAND)) return;
        if (!level.getBlockState(pos.above()).is(BlockTags.AIR)) return;

        // Vanilla skips tilling while you're raising a shield in your offhand: don't charge for that
        if (event.getHand() == InteractionHand.MAIN_HAND && player.getOffhandItem().has(DataComponents.BLOCKS_ATTACKS)
                && !player.isSecondaryUseActive()) return;

        ItemStack boneMeal = findBoneMeal(player);
        if (boneMeal.isEmpty()) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.literal("You need Bone Meal to till"));
            }
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
            return;
        }

        if (!level.isClientSide()) {
            boneMeal.shrink(1); // the hoe then tills as normal
        }
    }

    private static ItemStack findBoneMeal(Player player)
    {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(Items.BONE_MEAL)) return stack;
        }
        return ItemStack.EMPTY;
    }

    // Hoes lose durability on leaf litter, which vanilla skips because it breaks instantly
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBreakLeafLitter(BreakBlockEvent event)
    {
        if (event.getLevel().isClientSide()) return;

        Player player = event.getPlayer();
        ItemStack tool = player.getMainHandItem();
        if (event.getState().is(Blocks.LEAF_LITTER) && tool.is(ItemTags.HOES)) {
            tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
    }
}
