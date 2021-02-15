package mcjty.rftoolsstorage.modules.modularstorage.blocks;

import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.bindings.DefaultAction;
import mcjty.lib.bindings.IAction;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Cached;
import mcjty.rftoolsbase.api.compat.JEIRecipeAcceptor;
import mcjty.rftoolsbase.api.storage.IInventoryTracker;
import mcjty.rftoolsbase.api.storage.IModularStorage;
import mcjty.rftoolsbase.modules.filter.items.FilterModuleItem;
import mcjty.rftoolsstorage.craftinggrid.*;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageModule;
import mcjty.rftoolsstorage.modules.modularstorage.items.StorageModuleItem;
import mcjty.rftoolsstorage.storage.GlobalStorageItemWrapper;
import mcjty.rftoolsstorage.storage.StorageInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer.SLOT_FILTER_MODULE;
import static mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer.SLOT_STORAGE_MODULE;

public class ModularStorageTileEntity extends GenericTileEntity implements IInventoryTracker,
        CraftingGridProvider, JEIRecipeAcceptor, IModularStorage {

    public static final String CMD_SETTINGS = "storage.settings";
    public static final Key<String> PARAM_FILTER = new Key<>("filter", Type.STRING);
    public static final Key<String> PARAM_VIEWMODE = new Key<>("viewmode", Type.STRING);
    public static final Key<String> PARAM_SORTMODE = new Key<>("sortmode", Type.STRING);
    public static final Key<Boolean> PARAM_GROUPMODE = new Key<>("groupmode", Type.BOOLEAN);
    public static final Key<Boolean> PARAM_LOCKED = new Key<>("locked", Type.BOOLEAN);

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

    private final Cached<Predicate<ItemStack>> filterCache = Cached.of(this::createFilterCache);

    private final LazyOptional<IItemHandler> globalHandler = LazyOptional.of(this::createGlobalHandler);
    private final LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<ModularStorageContainer>("Modular Storage")
            .containerSupplier((windowId,player) -> new ModularStorageContainer(windowId, getPos(), player, ModularStorageTileEntity.this))
            .itemHandler(() -> getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(h -> h).orElseThrow(RuntimeException::new)));

    private GlobalStorageItemWrapper globalWrapper;
    private final ItemStackHandler cardHandler = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot == SLOT_STORAGE_MODULE) {
                if (globalWrapper != null) {
                    StorageInfo info = getStorageInfo();
                    globalWrapper.setInfo(info);
                }
            } else if (slot == SLOT_FILTER_MODULE) {
                filterCache.clear();
            }
            markDirtyClient();
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (locked) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (locked) {
                return ItemStack.EMPTY;
            }
            return super.extractItem(slot, amount, simulate);
        }
    };

    private final CraftingGrid craftingGrid = new CraftingGrid();

    private String sortMode = "";
    private String viewMode = "";
    private boolean groupMode = false;
    private String filter = "";
    private boolean locked = false;

    public ModularStorageTileEntity() {
        super(ModularStorageModule.TYPE_MODULAR_STORAGE.get());
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
        globalHandler.ifPresent(h -> itemSource.add(h, 0));

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
    public void syncInventoryFromServer(String sortMode, String viewMode, boolean groupMode, String filter, boolean locked) {
        this.sortMode = sortMode;
        this.viewMode = viewMode;
        this.groupMode = groupMode;
        this.filter = filter;
        this.locked = locked;
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);

        sortMode = tagCompound.getString("sortMode");
        viewMode = tagCompound.getString("viewMode");
        groupMode = tagCompound.getBoolean("groupMode");
        filter = tagCompound.getString("filter");
        craftingGrid.readFromNBT(tagCompound.getCompound("grid"));
    }

    @Override
    protected void readCaps(CompoundNBT tagCompound) {
        // We don't want this
    }

    @Override
    protected void readInfo(CompoundNBT tagCompound) {
        super.readInfo(tagCompound);
        if (tagCompound.contains("Info")) {
            CompoundNBT infoTag = tagCompound.getCompound("Info");
            cardHandler.deserializeNBT(infoTag.getCompound("Cards"));

            if (infoTag.contains("locked")) {
                locked = infoTag.getBoolean("locked");
            } else {
                // Old storage. Set a reasonable default based on the presence of a card in the slot
                if (cardHandler.getStackInSlot(SLOT_STORAGE_MODULE).isEmpty()) {
                    // No storage card, initialize locked to false
                    locked = false;
                } else {
                    locked = true;
                }
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);

        tagCompound.putString("sortMode", sortMode);
        tagCompound.putString("viewMode", viewMode);
        tagCompound.putBoolean("groupMode", groupMode);
        tagCompound.putString("filter", filter);
        tagCompound.put("grid", craftingGrid.writeToNBT());
        return tagCompound;
    }

    @Override
    protected void writeCaps(CompoundNBT tagCompound) {
        // We don't want this
    }

    @Override
    protected void writeInfo(CompoundNBT tagCompound) {
        super.writeInfo(tagCompound);
        CompoundNBT infoTag = getOrCreateInfo(tagCompound);
        infoTag.put("Cards", cardHandler.serializeNBT());
        infoTag.putBoolean("locked", locked);
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
            setLocked(params.get(PARAM_LOCKED));
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

    public void setLocked(boolean locked) {
        this.locked = locked;
        markDirtyClient();
    }

    public boolean isLocked() {
        return locked;
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

    @Nonnull
    public StorageInfo getStorageInfo() {
        ItemStack storageCard = cardHandler.getStackInSlot(SLOT_STORAGE_MODULE);
        if (storageCard.isEmpty()) {
            return StorageInfo.EMPTY;
        }
        Item item = storageCard.getItem();
        if (item instanceof StorageModuleItem) {
            UUID uuid = StorageModuleItem.getOrCreateUUID(storageCard);
            int version = StorageModuleItem.getVersion(storageCard);
            int size = StorageModuleItem.getSize(storageCard);
            String createdBy = StorageModuleItem.getCreatedBy(storageCard);
            return new StorageInfo(uuid, version, size, createdBy);
        }
        return StorageInfo.EMPTY;

    }

    @Override
    public int getVersion() {
        // @todo do we still need this?
        return getStorageInfo().getVersion();
    }

    public int getMaxSize() {
        StorageInfo info = getStorageInfo();
        return info.getSize();
    }

    //    @Override
//    public int getVersion() {
//        // @todo 1.14
//        if (isRemote()) {
//            RemoteStorageTileEntity storageTileEntity = getRemoteStorage(remoteId);
//            if (storageTileEntity == null) {
//                return version;
//            }
//            return storageTileEntity.getVersion();
//        } else {
//            return version;
//        }
//    }

    private Predicate<ItemStack> createFilterCache() {
        return FilterModuleItem.getCache(cardHandler.getStackInSlot(ModularStorageContainer.SLOT_FILTER_MODULE));
    }

    @Nonnull
    private IItemHandlerModifiable createGlobalHandler() {
        StorageInfo info = getStorageInfo();
        if (globalWrapper == null) {
            globalWrapper = new GlobalStorageItemWrapper(info, world.isRemote) {
                @Override
                public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                    boolean rc = super.isItemValid(slot, stack);
                    if (!rc) {
                        return false;
                    }
                    if (!cardHandler.getStackInSlot(ModularStorageContainer.SLOT_FILTER_MODULE).isEmpty()) {
                        if (filterCache.get() != null) {
                            return filterCache.get().test(stack);
                        }
                    }
                    return true;
                }

                @Nonnull
                @Override
                public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                    if (!locked) {
                        return stack;
                    }
                    return super.insertItem(slot, stack, simulate);
                }

                @Nonnull
                @Override
                public ItemStack extractItem(int slot, int amount, boolean simulate) {
                    if (!locked) {
                        return ItemStack.EMPTY;
                    }
                    return super.extractItem(slot, amount, simulate);
                }
            };
            if (!world.isRemote) {
                globalWrapper.setListener((version, slot) -> {
                    ItemStack storageSlot = cardHandler.getStackInSlot(SLOT_STORAGE_MODULE);
                    if (storageSlot.getItem() instanceof StorageModuleItem) {
                        storageSlot.getOrCreateTag().putInt("version", version);
                    }
                    markDirtyQuick();
                });
            }
        } else {
            globalWrapper.setInfo(info);
        }
        return globalWrapper;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return globalHandler.cast();
        }
        if (cap == CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY) {
            return screenHandler.cast();
        }
        return super.getCapability(cap, facing);
    }


    public IItemHandler getCardHandler() {
        return cardHandler;
    }

    public int getNumStacks() {
        int cnt = 0;
        if (globalWrapper == null) {
            createGlobalHandler();
        }
        for (int i = 0 ; i < globalWrapper.getSlots() ; i++) {
            if (!globalWrapper.getStackInSlot(i).isEmpty()) {
                cnt++;
            }
        }
        return cnt;
    }
}
