package mcjty.rftoolsstorage.modules.scanner.blocks;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.SlotDefinition;
import mcjty.rftoolsstorage.craftinggrid.CraftingGridInventory;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerSetup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

public class StorageScannerContainer extends GenericContainer {

    public static final String CONTAINER_GRID = "grid";

    public static final int SLOT_IN = 0;            // This slot is input for the user interface
    public static final int SLOT_OUT = 1;
    public static final int SLOT_IN_AUTO = 2;       // This slot is not shown in the user interface but is for automation
    public static final int SLOT_PLAYERINV = 2;

    public static final int SLOTS = 3;

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(SLOTS) {    // 1 extra slot for automation is at index 2
        @Override
        protected void setup() {
            slot(SlotDefinition.input(), CONTAINER_CONTAINER, SLOT_IN, 28, 220);
            slot(SlotDefinition.output(), CONTAINER_CONTAINER, SLOT_OUT, 55, 220);
            playerSlots(86, 162);
            gridSlots(CraftingGridInventory.GRID_XOFFSET, CraftingGridInventory.GRID_YOFFSET);
        }

        protected void gridSlots(int leftCol, int topRow) {
            box(SlotDefinition.ghost(), CONTAINER_GRID, CraftingGridInventory.SLOT_GHOSTINPUT, leftCol, topRow, 3, 3);
            topRow += 58;
            range(SlotDefinition.ghostOut(), CONTAINER_GRID, CraftingGridInventory.SLOT_GHOSTOUTPUT, leftCol, topRow, 1, 18);
        }
    };

    public void clearGrid() {
        IItemHandlerModifiable inventory = (IItemHandlerModifiable) inventories.get(CONTAINER_GRID);
        for (int i = 0 ; i < inventory.getSlots() ; i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    public StorageScannerContainer(int id, BlockPos pos, PlayerEntity player, StorageScannerTileEntity tileEntity) {
        super(StorageScannerSetup.CONTAINER_STORAGE_SCANNER.get(), id, CONTAINER_FACTORY, pos, tileEntity);
    }

    public StorageScannerContainer(ContainerType<StorageScannerContainer> type, int id, BlockPos pos, PlayerEntity player, StorageScannerTileEntity tileEntity) {
        super(type, id, CONTAINER_FACTORY, pos, tileEntity);
    }

    @Override
    public void setupInventories(IItemHandler itemHandler, PlayerInventory inventory) {
        addInventory(ContainerFactory.CONTAINER_CONTAINER, itemHandler);
        addInventory(ContainerFactory.CONTAINER_PLAYER, new InvWrapper(inventory));
        addInventory(CONTAINER_GRID, ((StorageScannerTileEntity) te).getCraftingGrid().getCraftingGridInventory());
        generateSlots();
    }

//    public StorageScannerContainer(EntityPlayer player, IInventory containerInventory) {
//        super(factory);
//        storageScannerTileEntity = (StorageScannerTileEntity) containerInventory;
//
//        addInventory(CONTAINER_INVENTORY, containerInventory);
//        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
//        addInventory(CONTAINER_GRID, storageScannerTileEntity.getCraftingGrid().getCraftingGridInventory());
//        generateSlots();
//    }
//
//    public StorageScannerContainer(EntityPlayer player, IInventory containerInventory, CraftingGridProvider provider) {
//        super(factory);
//        storageScannerTileEntity = (StorageScannerTileEntity) containerInventory;
//
//        addInventory(CONTAINER_INVENTORY, containerInventory);
//        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
//        addInventory(CONTAINER_GRID, provider.getCraftingGrid().getCraftingGridInventory());
//        generateSlots();
//    }
}
