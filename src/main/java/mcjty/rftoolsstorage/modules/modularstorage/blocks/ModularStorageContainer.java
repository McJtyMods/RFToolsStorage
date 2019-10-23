package mcjty.rftoolsstorage.modules.modularstorage.blocks;

import mcjty.lib.container.*;
import mcjty.rftoolsstorage.craftinggrid.CraftingGridInventory;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageSetup;
import mcjty.rftoolsstorage.modules.modularstorage.items.StorageModuleItem;
import mcjty.rftoolsstorage.modules.modularstorage.network.PacketSyncSlotsToClient;
import mcjty.rftoolsstorage.network.RFToolsStorageMessages;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class ModularStorageContainer extends GenericContainer {
    public static final String CONTAINER_GRID = "grid";
    public static final String CONTAINER_CARDS = "cards";       // The three cards

    public static final int SLOT_STORAGE_MODULE = 0;
    public static final int SLOT_TYPE_MODULE = 1;
    public static final int SLOT_FILTER_MODULE = 2;
    public static final int SLOT_STORAGE = 3;
    public static final int MAXSIZE_STORAGE = 300;

    public static final ContainerFactory factory = new ContainerFactory(SLOT_STORAGE) {
        @Override
        protected void setup() {
            slot(SlotDefinition.specific(stack -> stack.getItem() instanceof StorageModuleItem), CONTAINER_CARDS, SLOT_STORAGE_MODULE, 5, 157);
            slot(SlotDefinition.specific(stack -> false /* @todo 1.14 StorageTypeItem.class*/), CONTAINER_CARDS, SLOT_TYPE_MODULE, 5, 175);
            slot(SlotDefinition.specific(stack -> false /* @todo 1.14 StorageFilterItem.class*/), CONTAINER_CARDS, SLOT_FILTER_MODULE, 5, 193);
            box(SlotDefinition.input(), CONTAINER_CONTAINER, 0 /*SLOT_STORAGE*/, -500, -500, 100 /* @todo 1.14 should be actual size of inventory*/, 0, 1, 0); // Dummy slot positions
            playerSlots(91, 157);
            gridSlots(CraftingGridInventory.GRID_XOFFSET, CraftingGridInventory.GRID_YOFFSET);
        }

        protected void gridSlots(int leftCol, int topRow) {
            box(SlotDefinition.ghost(), CONTAINER_GRID, CraftingGridInventory.SLOT_GHOSTINPUT, leftCol, topRow, 3, 3);
            topRow += 58;
            range(SlotDefinition.ghostOut(), CONTAINER_GRID, CraftingGridInventory.SLOT_GHOSTOUTPUT, leftCol, topRow, 1, 18);
        }

    };

    public ModularStorageContainer(int id, BlockPos pos, PlayerEntity player, ModularStorageTileEntity tileEntity) {
        super(ModularStorageSetup.CONTAINER_MODULAR_STORAGE, id, factory, pos, tileEntity);
    }

    @Override
    public void setupInventories(IItemHandler itemHandler, PlayerInventory inventory) {
        ModularStorageTileEntity modularStorageTileEntity = (ModularStorageTileEntity) te;
        addInventory(CONTAINER_CARDS, modularStorageTileEntity.getCardHandler());        // The three cards
        addInventory(ContainerFactory.CONTAINER_CONTAINER, itemHandler);        // The storage card itemhandler
        addInventory(ContainerFactory.CONTAINER_PLAYER, new InvWrapper(inventory));
        addInventory(CONTAINER_GRID, modularStorageTileEntity.getCraftingGrid().getCraftingGridInventory());
        generateSlots();
    }

    @Override
    public void generateSlots() {
        for (SlotFactory slotFactory : factory.getSlots()) {
            Slot slot;
            if (CONTAINER_GRID.equals(slotFactory.getInventoryName()) || CONTAINER_CARDS.equals(slotFactory.getInventoryName())) {
                SlotType slotType = slotFactory.getSlotType();
                IItemHandler inventory = this.inventories.get(slotFactory.getInventoryName());
                int index = slotFactory.getIndex();
                int x = slotFactory.getX();
                int y = slotFactory.getY();
                slot = this.createSlot(slotFactory, inventory, index, x, y, slotType);
            } else if (slotFactory.getSlotType() == SlotType.SLOT_SPECIFICITEM) {
                final SlotDefinition slotDefinition = slotFactory.getSlotDefinition();
                slot = new SlotItemHandler(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY()) {
                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        return slotDefinition.itemStackMatches(stack);
                    }
                };
            } else if (slotFactory.getSlotType() == SlotType.SLOT_PLAYERINV || slotFactory.getSlotType() == SlotType.SLOT_PLAYERHOTBAR) {
                slot = new BaseSlot(inventories.get(slotFactory.getInventoryName()), te, slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY());
            } else {
                slot = new BaseSlot(inventories.get(slotFactory.getInventoryName()), te, slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY());
            }
            addSlot(slot);
        }
    }

    @Override
    public void putStackInSlot(int slotID, ItemStack stack) {
        super.putStackInSlot(slotID, stack);
    }

    @Override
    public ItemStack slotClick(int index, int button, ClickType mode, PlayerEntity player) {
        if (index == SLOT_STORAGE_MODULE && !player.getEntityWorld().isRemote) {
            // @todo 1.14
//            modularStorageTileEntity.copyToModule();
        }
        return super.slotClick(index, button, mode, player);
    }

    @Override
    public void detectAndSendChanges() {
        List<Pair<Integer, ItemStack>> differentSlots = new ArrayList<>();
        for (int i = 0; i < this.inventorySlots.size(); ++i) {
            ItemStack itemstack = this.inventorySlots.get(i).getStack();
            ItemStack itemstack1 = inventoryItemStacks.get(i);

            if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
                itemstack1 = itemstack.isEmpty() ? ItemStack.EMPTY : itemstack.copy();
                inventoryItemStacks.set(i, itemstack1);
                differentSlots.add(Pair.of(i, itemstack));
                if (differentSlots.size() >= 30) {
                    syncSlotsToListeners(differentSlots);
                    // Make a new list so that the one we gave to syncSlots is preserved
                    differentSlots = new ArrayList<>();
                }
            }
        }
        if (!differentSlots.isEmpty()) {
            syncSlotsToListeners(differentSlots);
        }
    }

    private void syncSlotsToListeners(List<Pair<Integer, ItemStack>> differentSlots) {
        ModularStorageTileEntity modularStorageTileEntity = (ModularStorageTileEntity) te;
        String sortMode = modularStorageTileEntity.getSortMode();
        String viewMode = modularStorageTileEntity.getViewMode();
        boolean groupMode = modularStorageTileEntity.isGroupMode();
        String filter = modularStorageTileEntity.getFilter();

        for (IContainerListener listener : this.listeners) {
            if (listener instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) listener;
                RFToolsStorageMessages.INSTANCE.sendTo(new PacketSyncSlotsToClient(
                        modularStorageTileEntity.getPos(),
                        sortMode, viewMode, groupMode, filter,
                        differentSlots), ((ServerPlayerEntity)player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
            }
        }
    }


//    @Override
//    public void detectAndSendChanges() {
//        super.detectAndSendChanges();
//    }
}
