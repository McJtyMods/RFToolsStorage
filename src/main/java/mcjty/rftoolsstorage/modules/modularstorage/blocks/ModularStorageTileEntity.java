package mcjty.rftoolsstorage.modules.modularstorage.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.container.ItemInventory;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.setup.Registration;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.Cached;
import mcjty.lib.varia.Sync;
import mcjty.rftoolsbase.api.compat.JEIRecipeAcceptor;
import mcjty.rftoolsbase.api.storage.IInventoryTracker;
import mcjty.rftoolsbase.api.storage.IModularStorage;
import mcjty.rftoolsbase.modules.filter.items.FilterModuleItem;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.craftinggrid.*;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageModule;
import mcjty.rftoolsstorage.modules.modularstorage.data.ModularStorageData;
import mcjty.rftoolsstorage.modules.modularstorage.data.StorageModuleData;
import mcjty.rftoolsstorage.modules.modularstorage.items.StorageModuleItem;
import mcjty.rftoolsstorage.storage.GlobalStorageItemWrapper;
import mcjty.rftoolsstorage.storage.StorageEntry;
import mcjty.rftoolsstorage.storage.StorageInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer.SLOT_FILTER_MODULE;
import static mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer.SLOT_STORAGE_MODULE;

public class ModularStorageTileEntity extends GenericTileEntity implements IInventoryTracker,
        CraftingGridProvider, JEIRecipeAcceptor, IModularStorage {

    private final Cached<Predicate<ItemStack>> filterCache = Cached.of(this::createFilterCache);

    private final ItemStackHandler cardHandler = createCardHandler();

    private final Lazy<IItemHandlerModifiable> items = Lazy.of(this::createGlobalHandler);
    @Cap(type = CapType.ITEMS)
    private static final Function<ModularStorageTileEntity, IItemHandlerModifiable> ITEM_CAP = tile -> tile.items.get();

    @Cap(type = CapType.CONTAINER)
    private static final Function<ModularStorageTileEntity, MenuProvider> screenHandler = be -> new DefaultContainerProvider<ModularStorageContainer>("Modular Storage")
            .containerSupplier((windowId, player) -> new ModularStorageContainer(windowId, be.getBlockPos(), be, player))
            .itemHandler(be.items)
//            .dataListener(Sync.string(ResourceLocation.fromNamespaceAndPath(RFToolsStorage.MODID, "settings_viewmode"), be::getViewMode, be::setViewMode))
            .setupSync(be);

    private GlobalStorageItemWrapper globalWrapper;

    private final CraftingGrid craftingGrid = new CraftingGrid();

    @GuiValue
    public static final Value<?, String> VALUE_SORTMODE = Value.create("sortMode", Type.STRING, ModularStorageTileEntity::getSortMode, ModularStorageTileEntity::setSortMode);
    private String sortMode = "";

    @GuiValue
    public static final Value<ModularStorageTileEntity, String> VALUE_VIEWMODE = Value.create("viewMode", Type.STRING, ModularStorageTileEntity::getViewMode, ModularStorageTileEntity::setViewMode);
    private String viewMode = "";

    @GuiValue
    public static final Value<?, Boolean> VALUE_GROUPMODE = Value.create("groupMode", Type.BOOLEAN, ModularStorageTileEntity::isGroupMode, ModularStorageTileEntity::setGroupMode);
    private boolean groupMode = false;

    @GuiValue
    public static final Value<?, String> VALUE_FILTER = Value.create("filter", Type.STRING, ModularStorageTileEntity::getFilter, ModularStorageTileEntity::setFilter);
    private String filter = "";

    public ModularStorageTileEntity(BlockPos pos, BlockState state) {
        super(ModularStorageModule.MODULAR_STORAGE.be().get(), pos, state);
    }

    public IItemHandlerModifiable getItems() {
        return items.get();
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
        itemSource.add(items.get(), 0);

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


    /**
     * Called from the container (detectAndSendChanges) and executed on the client.
     */
    public void syncInventoryFromServer(String sortMode, String viewMode, boolean groupMode, String filter, boolean locked) {
        this.sortMode = sortMode;
        this.viewMode = viewMode;
        this.groupMode = groupMode;
        this.filter = filter;
        ModularStorageData data = getData(ModularStorageModule.MODULAR_STORAGE_DATA);
        setData(ModularStorageModule.MODULAR_STORAGE_DATA, data.withLocked(locked));
    }

    @Override
    public void loadAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        super.loadAdditional(tagCompound, provider);
        for (int i = 0; i < cardHandler.getSlots(); i++) {
            cardHandler.setStackInSlot(i, ItemStack.parseOptional(provider, tagCompound.getCompound("slot" + i)));
        }
        sortMode = tagCompound.getString("sortMode");
        viewMode = tagCompound.getString("viewMode");
        groupMode = tagCompound.getBoolean("groupMode");
        filter = tagCompound.getString("filter");
        craftingGrid.readFromNBT(tagCompound.getCompound("grid"), provider);
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tagCompound, HolderLookup.Provider provider) {
        super.saveAdditional(tagCompound, provider);
        for (int i = 0; i < cardHandler.getSlots(); i++) {
            tagCompound.put("slot" + i, cardHandler.getStackInSlot(i).saveOptional(provider));
        }
        tagCompound.putString("sortMode", sortMode);
        tagCompound.putString("viewMode", viewMode);
        tagCompound.putBoolean("groupMode", groupMode);
        tagCompound.putString("filter", filter);
        tagCompound.put("grid", craftingGrid.writeToNBT(provider));
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        var data = input.get(ModularStorageModule.ITEM_MODULAR_STORAGE_DATA);
        if (data != null) {
            setData(ModularStorageModule.MODULAR_STORAGE_DATA, data);
        }
        var items = input.get(Registration.ITEM_INVENTORY);
        if (items != null) {
            for (int i = 0; i < items.items().size(); i++) {
                ItemStack stack = items.items().get(i);
                cardHandler.setStackInSlot(i, stack);
            }
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(ModularStorageModule.ITEM_MODULAR_STORAGE_DATA, getData(ModularStorageModule.MODULAR_STORAGE_DATA));
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < cardHandler.getSlots(); i++) {
            ItemStack stack = cardHandler.getStackInSlot(i);
            stacks.add(stack);
        }
        builder.set(Registration.ITEM_INVENTORY, new ItemInventory(stacks));
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
        ModularStorageData data = getData(ModularStorageModule.MODULAR_STORAGE_DATA);
        setData(ModularStorageModule.MODULAR_STORAGE_DATA, data.withLocked(locked));
        // Update the settings on the card
        if (!level.isClientSide) {
            ItemStack card = cardHandler.getStackInSlot(SLOT_STORAGE_MODULE);
            if (!card.isEmpty()) {
                StorageModuleData cardData = StorageModuleItem.getData(card);
                // Helper for client side tooltip
                cardData = cardData.withInfoAmount(getNumStacks());
                StorageEntry storage = globalWrapper.getStorage();
                if (storage != null) {
                    cardData = cardData.withCreationTime(storage.getCreationTime());
                    cardData = cardData.withUpdateTime(storage.getUpdateTime());
                }
                card.set(ModularStorageModule.ITEM_STORAGE_MODULE_DATA, cardData);
            }
        }
        setChanged();
    }

    public boolean isLocked() {
        ModularStorageData data = getData(ModularStorageModule.MODULAR_STORAGE_DATA);
        return data.locked();
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
        if (!isLocked()) {
            return;
        }
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

    private @NotNull ItemStackHandler createCardHandler() {
        return new ItemStackHandler(3) {
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
                if (isLocked()) {
                    return stack;
                }
                return super.insertItem(slot, stack, simulate);
            }

            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (isLocked()) {
                    return ItemStack.EMPTY;
                }
                return super.extractItem(slot, amount, simulate);
            }
        };
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
                    if (!isLocked()) {
                        return stack;
                    }
                    return super.insertItem(slot, stack, simulate);
                }

                @Nonnull
                @Override
                public ItemStack extractItem(int slot, int amount, boolean simulate) {
                    if (!isLocked()) {
                        return ItemStack.EMPTY;
                    }
                    return super.extractItem(slot, amount, simulate);
                }
            };
            if (!level.isClientSide) {
                globalWrapper.setListener((version, slot) -> {
                    ItemStack storageSlot = cardHandler.getStackInSlot(SLOT_STORAGE_MODULE);
                    if (storageSlot.getItem() instanceof StorageModuleItem) {
                        StorageModuleData data = StorageModuleItem.getData(storageSlot);
                        data = data.withVersion(version);
                        storageSlot.set(ModularStorageModule.ITEM_STORAGE_MODULE_DATA, data);
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
