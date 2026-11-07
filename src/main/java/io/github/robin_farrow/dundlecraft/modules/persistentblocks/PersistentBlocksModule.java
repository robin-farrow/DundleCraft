package io.github.robin_farrow.dundlecraft.modules.persistentblocks;

import com.mojang.serialization.Codec;
import io.github.robin_farrow.dundlecraft.DundleCraft;
import io.github.robin_farrow.dundlecraft.core.DundleModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class PersistentBlocksModule extends DundleModule
{
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, DundleCraft.MODID);

    // Ticks until a cargo entity next drops a heavy block; only counts down while the entity is loaded
    public static final Supplier<AttachmentType<Integer>> HEAVY_CARGO_TICKS_LEFT = ATTACHMENTS.register("heavy_cargo_ticks_left", () ->
            AttachmentType.builder(() -> PersistentBlocksRules.HEAVY_CARGO_DROP_TICKS)
                    .serialize(Codec.INT.fieldOf("ticks_left"))
                    .build());

    @Override
    public void register(IEventBus modEventBus)
    {
        modEventBus.addListener(this::onModifyComponents);
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new PersistentCargoDropEvents());
        ATTACHMENTS.register(modEventBus);
        modEventBus.addListener(this::onCommonSetup);
        NeoForge.EVENT_BUS.register(new ShearingEvents());
        modEventBus.addListener(this::onRegister);
    }

    // Every block is registered by the time items are, on client and server, and before anything uses the lists
    private void onRegister(RegisterEvent event)
    {
        if (event.getRegistryKey().equals(Registries.ITEM))
            PersistentBlocksRules.init();
    }

    // Sets the default max stack size of every heavy block item to 1
    private void onModifyComponents(ModifyDefaultComponentsEvent event)
    {
        event.modifyMatching(
                (item, components) -> item instanceof BlockItem blockItem && PersistentBlocksRules.isHeavy(blockItem.getBlock()),
                (components, context, item) -> components.set(DataComponents.MAX_STACK_SIZE, 1));
    }

    private void onCommonSetup(FMLCommonSetupEvent event)
    {
        // Dispensers no longer use shears; they keep them and click as if empty
        event.enqueueWork(() -> DispenserBlock.registerBehavior(Items.SHEARS, new OptionalDispenseItemBehavior()
        {
            @Override
            protected ItemStack execute(BlockSource source, ItemStack stack)
            {
                this.setSuccess(false);
                return stack;
            }
        }));
    }

    // Persistent drops go straight into the inventory if there's room; persistent ones that don't fit fall straight down
    @SubscribeEvent
    public void onPersistentBlockDrops(BlockDropsEvent event)
    {
        if (!(event.getBreaker() instanceof ServerPlayer player) || !PersistentBlocksRules.isPersistent(event.getState().getBlock()))
        {
            return;
        }

        Inventory inventory = player.getInventory();
        boolean hadEmptySlot = InventoryFullWarning.hasEmptySlot(inventory);
        List<ItemStack> leftovers = new ArrayList<>();

        event.getDrops().removeIf(drop ->
        {
            ItemStack stack = drop.getItem();
            if (!PersistentBlocksRules.isPersistent(stack))
            {
                return false;
            }

            int before = stack.getCount();
            inventory.add(stack);  // one per empty slot, shrinking the stack by however many fitted
            if (stack.getCount() < before)
            {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, 1.0F);
            }
            if (stack.isEmpty())
            {
                return true;
            }

            // No room: heavy-only blocks, or ones carrying their own data, drop as items
            if (PersistentFallingHandler.fallingStateFor(stack) == null)
            {
                return false;
            }
            leftovers.add(stack);
            return true;
        });

        ServerLevel level = event.getLevel();
        BlockState broken = event.getState();
        Vec3 blockCentre = Vec3.atBottomCenterOf(event.getPos());

        // Neither half of a double slab fitted: it falls as the one double slab it was.
        // The loot table splits it into two single slabs, so count what's left of the broken block's item
        Item brokenItem = broken.getBlock().asItem();
        int slabsLeft = 0;
        for (ItemStack stack : leftovers)
        {
            if (stack.is(brokenItem))
            {
                slabsLeft += stack.getCount();
            }
        }
        if (PersistentFallingHandler.isDoubleSlab(broken) && slabsLeft == 2)
        {
            leftovers.removeIf(stack -> stack.is(brokenItem));
            PersistentFallingHandler.spawn(level, blockCentre, broken.getBlock().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE), Vec3.ZERO, null);
        }

        for (ItemStack stack : leftovers)
        {
            BlockState falling = PersistentFallingHandler.keepOrientation(PersistentFallingHandler.fallingStateFor(stack), broken);
            for (int i = 0; i < stack.getCount(); i++)
            {
                PersistentFallingHandler.spawn(level, blockCentre, falling, Vec3.ZERO, null);
            }
        }

        InventoryFullWarning.warnIfJustFilled(inventory, hadEmptySlot);
    }

    // Holding a Persistent block, a shelf acts like any other block: you place against it instead of shelving it
    @SubscribeEvent
    public void onRightClickShelf(PlayerInteractEvent.RightClickBlock event)
    {
        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (state.getBlock() instanceof ShelfBlock && !state.getValue(ShelfBlock.POWERED)
                && PersistentBlocksRules.isPersistent(event.getItemStack()))
        {
            event.setUseBlock(TriState.FALSE);
        }
    }

    // Allays won't take a Persistent block, so they never try to collect them either
    @SubscribeEvent
    public void onGiveAllay(PlayerInteractEvent.EntityInteract event)
    {
        if (event.getTarget() instanceof Allay allay
                && allay.getMainHandItem().isEmpty()
                && PersistentBlocksRules.isPersistent(event.getItemStack()))
        {
            event.setCancellationResult(InteractionResult.PASS);
            event.setCanceled(true);
        }
    }

    // Persistent blocks can't be put in item frames
    @SubscribeEvent
    public void onFillItemFrame(PlayerInteractEvent.EntityInteract event)
    {
        if (event.getTarget() instanceof ItemFrame frame
                && frame.getItem().isEmpty()
                && PersistentBlocksRules.isPersistent(event.getItemStack()))
        {
            event.setCancellationResult(InteractionResult.PASS);
            event.setCanceled(true);
        }
    }

    // Persistent item entities become falling blocks with the same velocity, in a random orientation
    @SubscribeEvent
    public void onItemJoin(EntityJoinLevelEvent event)
    {
        if (!(event.getEntity() instanceof ItemEntity item) || !(event.getLevel() instanceof ServerLevel level))
        {
            return;
        }
        if (event.loadedFromDisk() || PersistentFallingHandler.isKeepingAsItem())
        {
            return;
        }

        BlockState state = PersistentFallingHandler.fallingStateFor(item.getItem());
        if (state == null)
        {
            return;
        }

        for (int i = 0; i < item.getItem().getCount(); i++)
        {
            Vec3 start = PersistentFallingHandler.unclip(level, item.position(), item.getDeltaMovement());
            Vec3 velocity = item.getDeltaMovement();

            // Buried (cargo dropping into its own pile, a death in a wall): start in the nearest free block space instead
            if (start == null)
            {
                BlockPos free = PersistentFallingHandler.nearestDestructible(level, BlockPos.containing(item.position()), null);
                start = free != null ? Vec3.atBottomCenterOf(free) : item.position();
                velocity = Vec3.ZERO;
            }

            PersistentFallingHandler.spawn(level, start, PersistentFallingHandler.randomOrientation(state, level.getRandom()), velocity, null);
        }
        event.setCanceled(true);
    }

    // Breaking a decorated pot puts a persistent block inside it straight into the inventory, before the pot itself
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBreakPotWithHeavyBlock(BreakBlockEvent event)
    {
        if (!(event.getPlayer() instanceof ServerPlayer player) || player.hasInfiniteMaterials())
        {
            return;
        }
        if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof DecoratedPotBlockEntity pot))
        {
            return;
        }

        ItemStack contents = pot.getTheItem();
        if (!PersistentBlocksRules.isPersistent(contents))
        {
            return;
        }

        Inventory inventory = player.getInventory();
        boolean hadEmptySlot = InventoryFullWarning.hasEmptySlot(inventory);
        ItemStack acquired = contents.copy();
        inventory.add(acquired);  // shrinks the copy by however much fitted

        if (acquired.getCount() < contents.getCount())
        {
            pot.setTheItem(acquired);  // anything that didn't fit stays in the pot and drops as normal
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, 1.0F);
            InventoryFullWarning.warnIfJustFilled(inventory, hadEmptySlot);
        }
    }
}
