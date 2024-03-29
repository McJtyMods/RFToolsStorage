package mcjty.rftoolsstorage.modules.modularstorage.blocks;

import mcjty.lib.container.*;
import mcjty.rftoolsbase.modules.filter.items.FilterModuleItem;
import mcjty.rftoolsstorage.craftinggrid.CraftingGridInventory;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageModule;
import mcjty.rftoolsstorage.modules.modularstorage.client.SlotOffsetCalculator;
import mcjty.rftoolsstorage.modules.modularstorage.items.StorageModuleItem;
import mcjty.rftoolsstorage.modules.modularstorage.network.PacketStorageInfoToClient;
import mcjty.rftoolsstorage.setup.RFToolsStorageMessages;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;

import static mcjty.lib.container.ContainerFactory.CONTAINER_CONTAINER;
import static mcjty.lib.container.SlotDefinition.*;

public class ModularStorageContainer extends GenericContainer {
    public static final String CONTAINER_GRID = "grid";
    public static final String CONTAINER_CARDS = "cards";       // The three cards

    public static final int SLOT_STORAGE_MODULE = 0;
    public static final int SLOT_TYPE_MODULE = 1;   // No longer supported
    public static final int SLOT_FILTER_MODULE = 2;
    public static final int SLOT_STORAGE = 3;
    public static final int MAXSIZE_STORAGE = 500;  // @todo, should be max of all possible storages

    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(SLOT_STORAGE)
            .slot(specific(stack -> stack.getItem() instanceof StorageModuleItem), CONTAINER_CARDS, SLOT_STORAGE_MODULE, 5, 157)
            .slot(specific(stack -> false /* @todo 1.14 StorageTypeItem.class*/), CONTAINER_CARDS, SLOT_TYPE_MODULE, -18, -18)  // No longer supported
            .slot(specific(stack -> stack.getItem() instanceof FilterModuleItem), CONTAINER_CARDS, SLOT_FILTER_MODULE, 5, 175)
            .box(generic().in(), CONTAINER_CONTAINER, 0 /*SLOT_STORAGE*/, -500, -500, 500 /* @todo 1.14 should be actual size of inventory*/, 0, 1, 0) // Dummy slot positions
            .playerSlots(91, 157)
            .box(ghost(), CONTAINER_GRID, CraftingGridInventory.SLOT_GHOSTINPUT, CraftingGridInventory.GRID_XOFFSET, CraftingGridInventory.GRID_YOFFSET, 3, 3)
            .range(ghostOut(), CONTAINER_GRID, CraftingGridInventory.SLOT_GHOSTOUTPUT, CraftingGridInventory.GRID_XOFFSET, CraftingGridInventory.GRID_YOFFSET + 58, 1, 18));

    public ModularStorageContainer(int id, BlockPos pos, ModularStorageTileEntity tileEntity, @Nonnull PlayerEntity player) {
        super(ModularStorageModule.CONTAINER_MODULAR_STORAGE.get(), id, CONTAINER_FACTORY.get(), pos, tileEntity, player);
    }

    @Override
    public void setupInventories(IItemHandler itemHandler, PlayerInventory inventory) {
        ModularStorageTileEntity modularStorageTileEntity = (ModularStorageTileEntity) te;
        addInventory(CONTAINER_CARDS, modularStorageTileEntity.getCardHandler());        // The three cards
        addInventory(ContainerFactory.CONTAINER_CONTAINER, itemHandler);        // The storage card itemhandler
        addInventory(ContainerFactory.CONTAINER_PLAYER, new InvWrapper(inventory));
        addInventory(CONTAINER_GRID, modularStorageTileEntity.getCraftingGrid().getCraftingGridInventory());
        generateSlots(inventory.player);
    }

    private int getAdjustedY(int y, boolean onClient) {
        if (onClient) {
            return y + SlotOffsetCalculator.getYOffset();
        }
        return y;
    }

    private boolean isLocked() {
        return ((ModularStorageTileEntity)te).isLocked();
    }

    @Override
    public void generateSlots(PlayerEntity player) {
        boolean onClient = getTe().getLevel().isClientSide();

        for (SlotFactory slotFactory : CONTAINER_FACTORY.get().getSlots()) {
            Slot slot;
            if (slotFactory.getSlotType() == SlotType.SLOT_GHOST) {
                slot = new GhostSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY()) {
                    @Override
                    public boolean mayPickup(PlayerEntity player) {
                        if (!isLocked()) {
                            return false;
                        }
                        return super.mayPickup(player);
                    }

                    @Override
                    public boolean mayPlace(@Nonnull ItemStack stack) {
                        if (!isLocked()) {
                            return false;
                        }
                        return super.mayPlace(stack);
                    }

                    @Override
                    public void set(ItemStack stack) {
                        if (!isLocked()) {
                            return;
                        }
                        super.set(stack);
                    }
                };
            } else if (slotFactory.getSlotType() == SlotType.SLOT_GHOSTOUT) {
                slot = new GhostOutputSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY()) {
                    @Override
                    public boolean mayPickup(PlayerEntity player) {
                        if (!isLocked()) {
                            return false;
                        }
                        return super.mayPickup(player);
                    }

                    @Override
                    public boolean mayPlace(@Nonnull ItemStack stack) {
                        if (!isLocked()) {
                            return false;
                        }
                        return super.mayPlace(stack);
                    }

                    @Override
                    public void set(@Nonnull ItemStack stack) {
                        if (!isLocked()) {
                            return;
                        }
                        super.set(stack);
                    }
                };
            } else if (slotFactory.getSlotType() == SlotType.SLOT_SPECIFICITEM) {
                final SlotDefinition slotDefinition = slotFactory.getSlotDefinition();
                slot = new SlotItemHandler(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(),
                        getAdjustedY(slotFactory.getY(), onClient)) {
                    @Override
                    public boolean mayPlace(@Nonnull ItemStack stack) {
                        if (isLocked()) {
                            return false;
                        }
                        return slotDefinition.itemStackMatches(stack);
                    }
                };
            } else if (slotFactory.getSlotType() == SlotType.SLOT_PLAYERINV || slotFactory.getSlotType() == SlotType.SLOT_PLAYERHOTBAR) {
                slot = new BaseSlot(inventories.get(slotFactory.getInventoryName()), te, slotFactory.getIndex(), slotFactory.getX(),
                        getAdjustedY(slotFactory.getY(), onClient));
            } else {
                slot = new BaseSlot(inventories.get(slotFactory.getInventoryName()), te, slotFactory.getIndex(), slotFactory.getX(),
                        getAdjustedY(slotFactory.getY(), onClient)) {
                    @Override
                    public boolean mayPlace(@Nonnull ItemStack stack) {
                        if (!isLocked()) {
                            return false;
                        }
                        return getItemHandler().isItemValid((((Slot)this).index) - 3, stack);
                    }
                };
            }
            addSlot(slot);
        }
    }

    @Override
    public void setItem(int slotID, @Nonnull ItemStack stack) {
        super.setItem(slotID, stack);
    }

    @Nonnull
    @Override
    public ItemStack clicked(int index, int button, @Nonnull ClickType mode, @Nonnull PlayerEntity player) {
        if (index == SLOT_STORAGE_MODULE && !player.getCommandSenderWorld().isClientSide) {
            // @todo 1.14
//            modularStorageTileEntity.copyToModule();
        }
        return super.clicked(index, button, mode, player);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        ModularStorageTileEntity modularStorageTileEntity = (ModularStorageTileEntity) te;
        String sortMode = modularStorageTileEntity.getSortMode();
        String viewMode = modularStorageTileEntity.getViewMode();
        boolean groupMode = modularStorageTileEntity.isGroupMode();
        String filter = modularStorageTileEntity.getFilter();
        boolean locked = modularStorageTileEntity.isLocked();

        for (IContainerListener listener : this.containerListeners) {
            if (listener instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) listener;
                RFToolsStorageMessages.INSTANCE.sendTo(new PacketStorageInfoToClient(
                        modularStorageTileEntity.getBlockPos(),
                        sortMode, viewMode, groupMode, filter, locked),
                        ((ServerPlayerEntity)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
        }
    }


    //    @Override
//    public void detectAndSendChanges() {
//        List<Pair<Integer, ItemStack>> differentSlots = new ArrayList<>();
//        for (int i = 0; i < this.inventorySlots.size(); ++i) {
//            ItemStack itemstack = this.inventorySlots.get(i).getStack();
//            ItemStack itemstack1 = inventoryItemStacks.get(i);
//
//            if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
//                itemstack1 = itemstack.isEmpty() ? ItemStack.EMPTY : itemstack.copy();
//                inventoryItemStacks.set(i, itemstack1);
//                differentSlots.add(Pair.of(i, itemstack));
//                if (differentSlots.size() >= 30) {
//                    syncSlotsToListeners(differentSlots);
//                    // Make a new list so that the one we gave to syncSlots is preserved
//                    differentSlots = new ArrayList<>();
//                }
//            }
//        }
//        if (!differentSlots.isEmpty()) {
//            syncSlotsToListeners(differentSlots);
//        }
//    }

//    private void syncSlotsToListeners(List<Pair<Integer, ItemStack>> differentSlots) {
//        ModularStorageTileEntity modularStorageTileEntity = (ModularStorageTileEntity) te;
//        String sortMode = modularStorageTileEntity.getSortMode();
//        String viewMode = modularStorageTileEntity.getViewMode();
//        boolean groupMode = modularStorageTileEntity.isGroupMode();
//        String filter = modularStorageTileEntity.getFilter();
//
//        for (IContainerListener listener : this.listeners) {
//            if (listener instanceof PlayerEntity) {
//                PlayerEntity player = (PlayerEntity) listener;
//                RFToolsStorageMessages.INSTANCE.sendTo(new PacketSyncSlotsToClient(
//                        modularStorageTileEntity.getPos(),
//                        sortMode, viewMode, groupMode, filter,
//                        differentSlots), ((ServerPlayerEntity)player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
//            }
//        }
//    }
}
