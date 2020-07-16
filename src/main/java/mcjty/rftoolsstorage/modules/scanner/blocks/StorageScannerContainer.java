package mcjty.rftoolsstorage.modules.scanner.blocks;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.rftoolsstorage.craftinggrid.CraftingGridInventory;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerSetup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

import static mcjty.lib.container.ContainerFactory.CONTAINER_CONTAINER;
import static mcjty.lib.container.SlotDefinition.*;

public class StorageScannerContainer extends GenericContainer {

    public static final String CONTAINER_GRID = "grid";

    public static final int SLOT_IN = 0;            // This slot is input for the user interface
    public static final int SLOT_OUT = 1;
    public static final int SLOT_IN_AUTO = 2;       // This slot is not shown in the user interface but is for automation
    public static final int SLOT_PLAYERINV = 2;

    public static final int SLOTS = 3;

    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(SLOTS)    // 1 extra slot for automation is at index 2
            .slot(input(), CONTAINER_CONTAINER, SLOT_IN, 28, 220)
            .slot(output(), CONTAINER_CONTAINER, SLOT_OUT, 55, 220)
            .playerSlots(86, 162)
            .box(ghost(), CONTAINER_GRID, CraftingGridInventory.SLOT_GHOSTINPUT, CraftingGridInventory.GRID_XOFFSET, CraftingGridInventory.GRID_YOFFSET, 3, 3)
            .range(ghostOut(), CONTAINER_GRID, CraftingGridInventory.SLOT_GHOSTOUTPUT, CraftingGridInventory.GRID_XOFFSET, CraftingGridInventory.GRID_YOFFSET + 58, 1, 18));

    public void clearGrid() {
        IItemHandlerModifiable inventory = (IItemHandlerModifiable) inventories.get(CONTAINER_GRID);
        for (int i = 0 ; i < inventory.getSlots() ; i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    protected StorageScannerContainer(ContainerType<StorageScannerContainer> type, int id, BlockPos pos, StorageScannerTileEntity tileEntity) {
        super(type, id, CONTAINER_FACTORY.get(), pos, tileEntity);
    }

    public static StorageScannerContainer create(int id, BlockPos pos, StorageScannerTileEntity tileEntity) {
        return new StorageScannerContainer(StorageScannerSetup.CONTAINER_STORAGE_SCANNER.get(), id, pos, tileEntity);
    }

    public static StorageScannerContainer createRemote(int id, BlockPos pos, StorageScannerTileEntity tileEntity) {
        return new RemoteStorageScannerContainer(StorageScannerSetup.CONTAINER_STORAGE_SCANNER_REMOTE.get(), id, pos, tileEntity);
    }

    protected boolean isRemoteContainer() {
        return false;
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        // If we are a remote container our canInteractWith should ignore distance
        if (isRemoteContainer()) {
            return te == null || !te.isRemoved();
        } else {
            return super.canInteractWith(player);
        }
    }

    @Override
    public void setupInventories(IItemHandler itemHandler, PlayerInventory inventory) {
        addInventory(ContainerFactory.CONTAINER_CONTAINER, itemHandler);
        addInventory(ContainerFactory.CONTAINER_PLAYER, new InvWrapper(inventory));
        addInventory(CONTAINER_GRID, ((StorageScannerTileEntity) te).getCraftingGrid().getCraftingGridInventory());
        generateSlots();
    }

}
