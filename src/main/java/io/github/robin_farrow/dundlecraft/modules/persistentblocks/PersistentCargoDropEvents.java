package io.github.robin_farrow.dundlecraft.modules.persistentblocks;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PersistentCargoDropEvents
{
    private static final int UPDATE_INTERVAL = 20;  // once a second

    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity.tickCount % UPDATE_INTERVAL != 0 || !(entity.level() instanceof ServerLevel level)) return;

        Container cargo = cargoOf(entity);
        if (cargo == null) return;

        // Held at full while a player rides it or has it open
        if (entity.hasPassenger(passenger -> passenger instanceof Player) || isOpenByPlayer(level, cargo)) {
            resetTimer(entity);
            return;
        }

        // Only runs while the entity is ticking, so the timer pauses in unloaded chunks
        int ticksLeft = entity.getData(PersistentBlocksModule.HEAVY_CARGO_TICKS_LEFT) - UPDATE_INTERVAL;
        if (ticksLeft > 0) {
            entity.setData(PersistentBlocksModule.HEAVY_CARGO_TICKS_LEFT, ticksLeft);
            return;
        }

        dropOnePersistent(level, entity, cargo);
        resetTimer(entity);
    }

    // Right-clicking a cargo entity (opening it, mounting it, feeding it) resets its timer
    @SubscribeEvent
    public void onInteract(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();
        if (!target.level().isClientSide() && cargoOf(target) != null)
            resetTimer(target);
    }

    private static void resetTimer(Entity entity) {
        entity.setData(PersistentBlocksModule.HEAVY_CARGO_TICKS_LEFT, PersistentBlocksRules.HEAVY_CARGO_DROP_TICKS);
    }

    private static @Nullable Container cargoOf(Entity entity) {
        if (entity instanceof AbstractChestedHorse horse)
            return horse.hasChest() ? horse.getInventory() : null;
        // Skip unopened loot carts, or reading them would generate their loot
        if (entity instanceof ContainerEntity container)
            return container.getContainerLootTable() == null ? container : null;
        return null;
    }

    // A player in this dimension has this cargo open in a screen
    private static boolean isOpenByPlayer(ServerLevel level, Container cargo) {
        for (ServerPlayer player : level.players()) {
            if (player.containerMenu == player.inventoryMenu) continue;  // only their own inventory is open
            for (Slot slot : player.containerMenu.slots)
                if (slot.container == cargo) return true;
        }
        return false;
    }

    // Drops one random persistent block at the entity's position.
    // Persistent blocks need somewhere to fall; if there's nowhere, they stay in the cargo
    private static void dropOnePersistent(ServerLevel level, Entity entity, Container cargo) {
        boolean roomToFall = PersistentFallingHandler.hasRoomToFall(level, entity.position());

        List<Integer> heavySlots = new ArrayList<>();
        for (int i = 0; i < cargo.getContainerSize(); i++) {
            ItemStack stack = cargo.getItem(i);
            if (PersistentBlocksRules.isPersistent(stack) && (roomToFall || PersistentFallingHandler.fallingStateFor(stack) == null))
                heavySlots.add(i);
        }
        if (heavySlots.isEmpty()) return;

        int slot = heavySlots.get(entity.getRandom().nextInt(heavySlots.size()));
        entity.spawnAtLocation(level, cargo.removeItem(slot, 1));
    }
}
