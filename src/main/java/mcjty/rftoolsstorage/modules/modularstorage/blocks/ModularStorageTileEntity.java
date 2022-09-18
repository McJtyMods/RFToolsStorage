package mcjty.rftoolsstorage.modules.modularstorage.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.Cached;
import mcjty.rftoolsbase.api.compat.JEIRecipeAcceptor;
import mcjty.rftoolsbase.api.storage.IInventoryTracker;
import mcjty.rftoolsbase.api.storage.IModularStorage;
import mcjty.rftoolsbase.modules.filter.items.FilterModuleItem;
import mcjty.rftoolsstorage.craftinggrid.*;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageModule;
import mcjty.rftoolsstorage.modules.modularstorage.items.StorageModuleItem;
import mcjty.rftoolsstorage.storage.GlobalStorageItemWrapper;
import mcjty.rftoolsstorage.storage.StorageEntry;
import mcjty.rftoolsstorage.storage.StorageInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.*;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer.SLOT_FILTER_MODULE;
import static mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer.SLOT_STORAGE_MODULE;

public class ModularStorageTileEntity extends GenericTileEntity implements IInventoryTracker,
        CraftingGridProvider, JEIRecipeAcceptor, IModularStorage {

    private final Cached<Predicate<ItemStack>> filterCache = Cached.of(this::createFilterCache);

    @Cap(type = CapType.ITEMS)
    private final LazyOptional<IItemHandler> globalHandler = LazyOptional.of(this::createGlobalHandler);
    @Cap(type = CapType.CONTAINER)
    private final LazyOptional<MenuProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<ModularStorageContainer>("Modular Storage")
            .containerSupplier((windowId, player) -> new ModularStorageContainer(windowId, getBlockPos(), this, player))
            .itemHandler(() -> getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(h -> h).orElseThrow(RuntimeException::new))
            .setupSync(this));

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
            setChanged();
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

    public ModularStorageTileEntity(BlockPos pos, BlockState state) {
        super(ModularStorageModule.TYPE_MODULAR_STORAGE.get(), pos, state);
    }

    @Override
    public void setRecipe(int index, ItemStack[] stacks) {
        craftingGrid.setRecipe(index, stacks);
        setChanged();
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
        setChanged();
    }

    @Override
    public CraftingGrid getCraftingGrid() {
        return craftingGrid;
    }

    @Override
    public void markInventoryDirty() {
        setChanged();
    }

    @Override
    @Nonnull
    public List<Pair<ItemStack, Integer>> craft(Player player, int n, boolean test) {
        InventoriesItemSource itemSource = new InventoriesItemSource().add(new InvWrapper(player.getInventory()), 0);
        globalHandler.ifPresent(h -> itemSource.add(h, 0));

        if (test) {
            return StorageCraftingTools.testCraftItems(player, n, craftingGrid.getActiveRecipe(), itemSource);
        } else {
            StorageCraftingTools.craftItems(player, n, craftingGrid.getActiveRecipe(), itemSource);
            // @todo 1.14
//            updateStackCount();
            return Collections.emptyList();
        }
    }

    public boolean isGroupMode() {
        return groupMode;
    }

    public void setGroupMode(boolean groupMode) {
        this.groupMode = groupMode;
        setChanged();
    }

    public String getSortMode() {
        return sortMode;
    }

    public void setSortMode(String sortMode) {
        this.sortMode = sortMode;
        setChanged();
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
        setChanged();
    }

    public String getViewMode() {
        return viewMode;
    }

    public void setViewMode(String viewMode) {
        this.viewMode = viewMode;
        setChanged();
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
    public void loadClientDataFromNBT(CompoundTag tagCompound) {
        // @todo 1.14
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
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);

        sortMode = tagCompound.getString("sortMode");
        viewMode = tagCompound.getString("viewMode");
        groupMode = tagCompound.getBoolean("groupMode");
        filter = tagCompound.getString("filter");
        craftingGrid.readFromNBT(tagCompound.getCompound("grid"));
    }

    @Override
    protected void loadCaps(CompoundTag tagCompound) {
        // We don't want this
    }

    @Override
    protected void loadInfo(CompoundTag tagCompound) {
        super.loadInfo(tagCompound);
        if (tagCompound.contains("Info")) {
            CompoundTag infoTag = tagCompound.getCompound("Info");
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
    public void saveAdditional(@Nonnull CompoundTag tagCompound) {
        super.saveAdditional(tagCompound);

        tagCompound.putString("sortMode", sortMode);
        tagCompound.putString("viewMode", viewMode);
        tagCompound.putBoolean("groupMode", groupMode);
        tagCompound.putString("filter", filter);
        tagCompound.put("grid", craftingGrid.writeToNBT());
    }

    @Override
    protected void saveCaps(CompoundTag tagCompound) {
        // We don't want this
    }

    @Override
    protected void saveInfo(CompoundTag tagCompound) {
        super.saveInfo(tagCompound);
        CompoundTag infoTag = getOrCreateInfo(tagCompound);
        infoTag.put("Cards", cardHandler.serializeNBT());
        infoTag.putBoolean("locked", locked);
    }

    public static final Key<String> PARAM_FILTER = new Key<>("filter", Type.STRING);
    public static final Key<String> PARAM_VIEWMODE = new Key<>("viewmode", Type.STRING);
    public static final Key<String> PARAM_SORTMODE = new Key<>("sortmode", Type.STRING);
    public static final Key<Boolean> PARAM_GROUPMODE = new Key<>("groupmode", Type.BOOLEAN);
    public static final Key<Boolean> PARAM_LOCKED = new Key<>("locked", Type.BOOLEAN);
    @ServerCommand
    public static final Command<?> CMD_SETTINGS = Command.<ModularStorageTileEntity>create("storage.settings",
            (te, player, params) -> {
                te.setFilter(params.get(PARAM_FILTER));
                te.setViewMode(params.get(PARAM_VIEWMODE));
                te.setSortMode(params.get(PARAM_SORTMODE));
                te.setGroupMode(params.get(PARAM_GROUPMODE));
                te.setLocked(params.get(PARAM_LOCKED));
                te.setChanged();
            });


    @ServerCommand
    public static final Command<?> CMD_CLEARGRID = Command.<ModularStorageTileEntity>create("clearGrid", (te, player, params) -> te.clearGrid());

    private void clearGrid() {
        CraftingGridInventory inventory = craftingGrid.getCraftingGridInventory();
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
        setChanged();
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        // Update the settings on the card
        if (!level.isClientSide) {
            ItemStack card = cardHandler.getStackInSlot(SLOT_STORAGE_MODULE);
            if (!card.isEmpty()) {
                // Helper for client side tooltip
                card.getOrCreateTag().putInt("infoAmount", getNumStacks());
                StorageEntry storage = globalWrapper.getStorage();
                if (storage != null) {
                    card.getOrCreateTag().putLong("infoCreateTime", storage.getCreationTime());
                    card.getOrCreateTag().putLong("infoUpdateTime", storage.getUpdateTime());
                }
            }
        }
        setChanged();
    }

    public boolean isLocked() {
        return locked;
    }

    @ServerCommand
    public static final Command<?> CMD_CYCLE = Command.<ModularStorageTileEntity>create("cycle", (te, player, params) -> te.cycle());

    private void cycle() {
        // @todo 1.14
//        if (isRemote()) {
//            RemoteStorageTileEntity storageTileEntity = getRemoteStorage(remoteId);
//            if (storageTileEntity == null) {
//                return;
//            }
//            remoteId = storageTileEntity.cycle(remoteId);
//            getStackInSlot(ModularStorageContainer.SLOT_STORAGE_MODULE).getTagCompound().setInteger("id", remoteId);
//            setChanged();
//        }
    }

    @ServerCommand
    public static final Command<?> CMD_COMPACT = Command.<ModularStorageTileEntity>create("compact", (te, player, params) -> te.compact());

    private void compact() {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0 ; i < globalWrapper.getSlots() ; i++) {
            ItemStack stack = globalWrapper.getStackInSlot(i);
            if (!stack.isEmpty()) {
                stacks.add(stack);
                globalWrapper.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
        for (ItemStack stack : stacks) {
            ItemHandlerHelper.insertItem(globalWrapper, stack, false);
        }
        setChanged();
    }

    @Nonnull
    public StorageInfo getStorageInfo() {
        ItemStack storageCard = cardHandler.getStackInSlot(SLOT_STORAGE_MODULE);
        if (storageCard.isEmpty()) {
            return StorageInfo.EMPTY;
        }
        return StorageModuleItem.getStorageInfo(storageCard);
    }

    @Override
    public int getVersion() {
        // @todo do we still need this?
        return getStorageInfo().version();
    }

    public int getMaxSize() {
        StorageInfo info = getStorageInfo();
        return info.size();
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
            globalWrapper = new GlobalStorageItemWrapper(info, level.isClientSide) {
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
            if (!level.isClientSide) {
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
