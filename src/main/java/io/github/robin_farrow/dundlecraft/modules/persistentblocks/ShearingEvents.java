package io.github.robin_farrow.dundlecraft.modules.persistentblocks;

import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.monster.cubemob.SulfurCube;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.IShearable;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public class ShearingEvents
{
    @SubscribeEvent
    public void onShear(PlayerInteractEvent.EntityInteract event)
    {
        ItemStack shears = event.getItemStack();
        if (!shears.is(Items.SHEARS))
        {
            return;
        }

        Entity target = event.getTarget();
        boolean isSheep = target instanceof Sheep;
        boolean isGolem = target instanceof SnowGolem;
        boolean isCube = target instanceof SulfurCube cube && !cube.isBaby() && !cube.isPrimed();
        if (!isSheep && !isGolem && !isCube)
        {
            return;
        }

        // Same check NeoForge's shears use (readyForShearing for these mobs)
        IShearable shearable = (IShearable) target;
        BlockPos pos = target.blockPosition();
        if (!shearable.isShearable(event.getEntity(), shears, target.level(), pos))
        {
            return;
        }

        // Handled here on both sides, so vanilla's own shearing never runs
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!(event.getEntity() instanceof ServerPlayer player) || !(target.level() instanceof ServerLevel level))
        {
            return;
        }

        // Sheep and sulfur cubes need a free slot; a snow golem's pumpkin just drops if there isn't one
        if (!isGolem && HeavyCrafting.freeSlots(player) < 1)
        {
            InventoryFullWarning.warnFull(player);
            return;
        }

        Inventory inventory = player.getInventory();
        boolean hadEmptySlot = InventoryFullWarning.hasEmptySlot(inventory);
        ItemStack body = target instanceof SulfurCube cube ? cube.getItemBySlot(EquipmentSlot.BODY).copy() : ItemStack.EMPTY;

        // Shears it and returns what it would have dropped, without spawning anything
        List<ItemStack> drops = shearable.onSheared(player, shears, level, pos);
        for (ItemStack drop : drops)
        {
            if (!inventory.add(drop))
            {
                shearable.spawnShearedDrop(level, pos, drop);   // no room: drops where vanilla would have put it
            }
        }

        if (target instanceof SulfurCube cube)
        {
            CriteriaTriggers.PLAYER_SHEARED_EQUIPMENT.trigger(player, body, cube);
        }
        target.gameEvent(GameEvent.SHEAR, player);
        shears.hurtAndBreak(1, player, event.getHand().asEquipmentSlot());
        InventoryFullWarning.warnIfJustFilled(inventory, hadEmptySlot);
    }
}
