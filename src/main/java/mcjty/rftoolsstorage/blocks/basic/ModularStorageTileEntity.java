package mcjty.rftoolsstorage.blocks.basic;

import mcjty.lib.bindings.DefaultAction;
import mcjty.lib.bindings.IAction;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsstorage.api.general.IInventoryTracker;
import mcjty.rftoolsstorage.blocks.ModBlocks;
import mcjty.rftoolsstorage.compat.jei.JEIRecipeAcceptor;
import mcjty.rftoolsstorage.craftinggrid.*;
import mcjty.rftoolsstorage.storage.StorageFilterCache;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ModularStorageTileEntity extends GenericTileEntity implements ITickableTileEntity, IInventoryTracker,
        CraftingGridProvider, JEIRecipeAcceptor {

    public static final String CMD_SETTINGS = "storage.settings";
    public static final Key<String> PARAM_FILTER = new Key<>("filter", Type.STRING);
    public static final Key<String> PARAM_VIEWMODE = new Key<>("viewmode", Type.STRING);
    public static final Key<String> PARAM_SORTMODE = new Key<>("sortmode", Type.STRING);
    public static final Key<Boolean> PARAM_GROUPMODE = new Key<>("groupmode", Type.BOOLEAN);

    public static final String ACTION_COMPACT = "compact";
    public static final String ACTION_CYCLE = "cycle";
    public static final String ACTION_CLEARGRID = "clearGrid";

    @Override
    public IAction[] getActions() {
        return new IAction[] {
                new DefaultAction(ACTION_COMPACT, this::compact),
                new DefaultAction(ACTION_CYCLE, this::cycle),
                new DefaultAction(ACTION_CLEARGRID, this::clearGrid),
        };
    }

    private StorageFilterCache filterCache = null;

    private LazyOptional<IItemHandler> emptyHandler = LazyOptional.of(() -> new EmptyHandler());
    private ItemStackHandler cardHandler = new ItemStackHandler(3);

    private CraftingGrid craftingGrid = new CraftingGrid();

    private String sortMode = "";
    private String viewMode = "";
    private boolean groupMode = false;
    private String filter = "";

    public ModularStorageTileEntity() {
        super(ModBlocks.TYPE_MODULAR_STORAGE);
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {

    }

    @Override
    public void setRecipe(int index, ItemStack[] stacks) {
        craftingGrid.setRecipe(index, stacks);
        markDirty();
    }

    @Override
    public void storeRecipe(int index) {
        getCraftingGrid().storeRecipe(index);
    }

    @Override
    public void setGridContents(List<ItemStack> stacks) {
        for (int i = 0 ; i < stacks.size() ; i++) {
            craftingGrid.getCraftingGridInventory().setStackInSlot(i, stacks.get(i));
        }
        markDirty();
    }

    @Override
    public CraftingGrid getCraftingGrid() {
        return craftingGrid;
    }

    @Override
    public void markInventoryDirty() {
        markDirty();
    }

    @Override
    @Nonnull
    public int[] craft(PlayerEntity player, int n, boolean test) {
        InventoriesItemSource itemSource = new InventoriesItemSource().add(new InvWrapper(player.inventory), 0);
        getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> itemSource.add(h, 0));

        if (test) {
            return StorageCraftingTools.testCraftItems(player, n, craftingGrid.getActiveRecipe(), itemSource);
        } else {
            StorageCraftingTools.craftItems(player, n, craftingGrid.getActiveRecipe(), itemSource);
            // @todo 1.14
//            updateStackCount();
            return new int[0];
        }
    }

    public boolean isGroupMode() {
        return groupMode;
    }

    public void setGroupMode(boolean groupMode) {
        this.groupMode = groupMode;
        markDirty();
    }

    public String getSortMode() {
        return sortMode;
    }

    public void setSortMode(String sortMode) {
        this.sortMode = sortMode;
        markDirty();
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
        markDirty();
    }

    public String getViewMode() {
        return viewMode;
    }

    public void setViewMode(String viewMode) {
        this.viewMode = viewMode;
        markDirty();
    }

    public int getRenderLevel() {
        // @todo 1.14
        return 0;
//        if (numStacks == -1 || maxSize == 0) {
//            return -1;
//        }
//        return (numStacks+6) * 7 / maxSize;
    }

    @Override
    public void readClientDataFromNBT(CompoundNBT tagCompound) {
        // @todo 1.14
    }



    @Override
    public void writeClientDataToNBT(CompoundNBT tagCompound) {
    }

    /**
     * Called from the container (detectAndSendChanges) and executed on the client.
     */
    public void syncInventoryFromServer(int maxSize, int numStacks, String sortMode, String viewMode, boolean groupMode, String filter) {
        this.sortMode = sortMode;
        this.viewMode = viewMode;
        this.groupMode = groupMode;
        this.filter = filter;
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);

        cardHandler.deserializeNBT(tagCompound.getCompound("Cards"));

        sortMode = tagCompound.getString("sortMode");
        viewMode = tagCompound.getString("viewMode");
        groupMode = tagCompound.getBoolean("groupMode");
        filter = tagCompound.getString("filter");
        craftingGrid.readFromNBT(tagCompound.getCompound("grid"));
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);

        tagCompound.put("Cards", cardHandler.serializeNBT());

        tagCompound.putString("sortMode", sortMode);
        tagCompound.putString("viewMode", viewMode);
        tagCompound.putBoolean("groupMode", groupMode);
        tagCompound.putString("filter", filter);
        tagCompound.put("grid", craftingGrid.writeToNBT());
        return tagCompound;
    }

    private void writeCardStack(CompoundNBT tagCompound, String cardName, ItemStack card) {
        CompoundNBT storageNBT = new CompoundNBT();
        card.write(storageNBT);
        tagCompound.put(cardName, storageNBT);
    }

    @Override
    public boolean execute(PlayerEntity playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_SETTINGS.equals(command)) {
            setFilter(params.get(PARAM_FILTER));
            setViewMode(params.get(PARAM_VIEWMODE));
            setSortMode(params.get(PARAM_SORTMODE));
            setGroupMode(params.get(PARAM_GROUPMODE));
            markDirtyClient();
            return true;
        }
        return false;
    }

    private void clearGrid() {
        CraftingGridInventory inventory = craftingGrid.getCraftingGridInventory();
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
        markDirty();
    }

    private void cycle() {
        // @todo 1.14
//        if (isRemote()) {
//            RemoteStorageTileEntity storageTileEntity = getRemoteStorage(remoteId);
//            if (storageTileEntity == null) {
//                return;
//            }
//            remoteId = storageTileEntity.cycle(remoteId);
//            getStackInSlot(ModularStorageContainer.SLOT_STORAGE_MODULE).getTagCompound().setInteger("id", remoteId);
//            markDirtyClient();
//        }
    }

    private void compact() {
        // @todo 1.14
//        if (isRemote()) {
//            RemoteStorageTileEntity storageTileEntity = getRemoteStorage(remoteId);
//            if (storageTileEntity == null) {
//                return;
//            }
//            storageTileEntity.compact(remoteId);
//        } else {
//            InventoryHelper.compactStacks(inventoryHelper, ModularStorageContainer.SLOT_STORAGE, maxSize);
//        }
//
//        updateStackCount();
//        markDirtyClient();
    }

    @Override
    public int getVersion() {
        // @todo 1.14
//        if (isRemote()) {
//            RemoteStorageTileEntity storageTileEntity = getRemoteStorage(remoteId);
//            if (storageTileEntity == null) {
//                return version;
//            }
//            return storageTileEntity.getVersion();
//        } else {
//            return version;
//        }
        return 0;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            ItemStack storageCard = cardHandler.getStackInSlot(ModularStorageContainer.SLOT_STORAGE_MODULE);
            if (storageCard.isEmpty()) {
                return emptyHandler.cast();
            }
            return storageCard.getCapability(cap, facing);
        }
        return super.getCapability(cap, facing);
    }


    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
        ModularStorageContainer container = new ModularStorageContainer(windowId, getPos(), player, this);
        getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> container.setupInventories(h, inventory));
//        energyHandler.ifPresent(e -> e.addIntegerListeners(container));
        return container;
    }

    public IItemHandler getCardHandler() {
        return cardHandler;
    }
}
