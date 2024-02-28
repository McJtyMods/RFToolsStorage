package mcjty.rftoolsstorage.modules.scanner.blocks;

import com.google.common.base.Function;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ResultCommand;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.*;
import mcjty.rftoolsbase.api.compat.JEIRecipeAcceptor;
import mcjty.rftoolsbase.api.infoscreen.CapabilityInformationScreenInfo;
import mcjty.rftoolsbase.api.infoscreen.IInformationScreenInfo;
import mcjty.rftoolsbase.api.storage.IInventoryTracker;
import mcjty.rftoolsbase.api.storage.IStorageScanner;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.craftinggrid.*;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerTileEntity;
import mcjty.rftoolsstorage.modules.craftingmanager.system.CraftingSystem;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerConfiguration;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerModule;
import mcjty.rftoolsstorage.modules.scanner.tools.CachedItemCount;
import mcjty.rftoolsstorage.modules.scanner.tools.CachedItemKey;
import mcjty.rftoolsstorage.modules.scanner.tools.InventoryAccessSettings;
import mcjty.rftoolsstorage.modules.scanner.tools.SortingMode;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.ForgeCapabilities;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static mcjty.lib.container.GenericItemHandler.slot;
import static mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerContainer.CONTAINER_FACTORY;

public class StorageScannerTileEntity extends TickingTileEntity implements CraftingGridProvider, JEIRecipeAcceptor, IStorageScanner {

    public static final Key<Integer> PARAM_INDEX = new Key<>("index", Type.INTEGER);
    public static final Key<BlockPos> PARAM_POS = new Key<>("pos", Type.BLOCKPOS);
    public static final Key<Boolean> PARAM_VIEW = new Key<>("view", Type.BOOLEAN);

    @GuiValue(name = "export")
    private boolean exportToCurrent = false;
    @GuiValue
    public static final Value<?, Integer> VALUE_RADIUS = Value.create("radius", Type.INTEGER, StorageScannerTileEntity::getRadius, StorageScannerTileEntity::setRadius);
    @GuiValue
    private SortingMode sortMode = SortingMode.NAME;

    // Client side data returned by CMD_SCANNER_INFO
    public long rfReceived = 0;
    public boolean exportToCurrentReceived = false;

    private final FakePlayerGetter lazyPlayer = new FakePlayerGetter(this, "rftools_storage");

    public static final int XNETDELAY = 40;

    private final CraftingSystem craftingSystem = new CraftingSystem(this);
    private List<BlockPos> inventories = new ArrayList<>();
    private List<BlockPos> craftingInventories = null; // Subset of 'inventories' with all the crafting managers
    private final Set<BlockPos> inventoriesFromXNet = new HashSet<>();

    // This data is fed directly by the storage channel system (XNet) and is
    // cleared automatically if that system stops or is disabled
    private Map<BlockPos, InventoryAccessSettings> xnetAccess = Collections.emptyMap();
    private int xnetDelay = XNETDELAY;      // Timer to control when to clear the above

    private final Map<CachedItemKey, CachedItemCount> cachedCounts = new HashMap<>();
    private final Set<BlockPos> routable = new HashSet<>();
    private int radius = 1;

    private BlockPos lastSelectedInventory = null;

    // Indicates if for this storage scanner the inventories should be shown wide
    @GuiValue
    private boolean openWideView = true;

    private final LazyOptional<IInformationScreenInfo> infoScreenInfo = LazyOptional.of(this::createScreenInfo);

    @Cap(type = CapType.ENERGY)
    private final GenericEnergyStorage energyStorage = new GenericEnergyStorage(this, true, StorageScannerConfiguration.MAXENERGY.get(), StorageScannerConfiguration.RECEIVEPERTICK.get());

    @Cap(type = CapType.ITEMS)
    private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY)
            .insertable(slot(StorageScannerContainer.SLOT_IN_AUTO))
            .build();

    @Cap(type = CapType.CONTAINER)
    private final Lazy<MenuProvider> screenHandler = Lazy.of(() -> new DefaultContainerProvider<StorageScannerContainer>("Storage Scanner")
            .containerSupplier((windowId, player) -> StorageScannerContainer.create(windowId, getBlockPos(), StorageScannerTileEntity.this, player))
            .energyHandler(() -> energyStorage)
            .itemHandler(() -> items)
            .setupSync(this));

    @Cap(type = CapType.INFUSABLE)
    private final IInfusable infusableHandler = new DefaultInfusable(StorageScannerTileEntity.this);

    private final CraftingGrid craftingGrid = new CraftingGrid();

    // If set this is a dummy tile entity
    private ResourceKey<Level> dummyType = null;

    public StorageScannerTileEntity(BlockPos pos, BlockState state) {
        super(StorageScannerModule.TYPE_STORAGE_SCANNER.get(), pos, state);
        radius = (StorageScannerConfiguration.xnetRequired.get() && RFToolsStorage.setup.xnet) ? 0 : 1;
    }

    // Used for a dummy tile entity (tablet usage)
    public StorageScannerTileEntity(ResourceKey<Level> type, BlockPos pos) {
        this(pos, StorageScannerModule.STORAGE_SCANNER.get().defaultBlockState());
        dummyType = type;
    }

    @Override
    public void storeRecipe(int index) {
        getCraftingGrid().storeRecipe(index);
    }

    @Override
    public void setRecipe(int index, ItemStack[] stacks) {
        craftingGrid.setRecipe(index, stacks);
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
        RFCraftingRecipe activeRecipe = craftingGrid.getActiveRecipe();
        return craft(player, n, test, activeRecipe);
    }

    @Nonnull
    public List<Pair<ItemStack, Integer>> craft(Player player, int n, boolean test, RFCraftingRecipe activeRecipe) {
        TileEntityItemSource itemSource = new TileEntityItemSource()
                .add(new InvWrapper(player.getInventory()), 0);
        inventories.stream()
                .filter(p -> isOutputFromGui(p) && isRoutable(p))
                .forEachOrdered(p -> {
                    BlockEntity tileEntity = level.getBlockEntity(p);
                    if (tileEntity instanceof CraftingManagerTileEntity) {
                        // @todo crafting manager
                    } else if (tileEntity != null && !(tileEntity instanceof StorageScannerTileEntity)) {
                        itemSource.add(tileEntity, 0);
                    }
                });

        if (test) {
            return StorageCraftingTools.testCraftItems(player, n, activeRecipe, itemSource);
        } else {
            StorageCraftingTools.craftItems(player, n, activeRecipe, itemSource);
            return Collections.emptyList();
        }
    }

    @Override
    public void setGridContents(List<ItemStack> stacks) {
        for (int i = 0; i < stacks.size(); i++) {
            craftingGrid.getCraftingGridInventory().setStackInSlot(i, stacks.get(i));
        }
        setChanged();
    }

    private long getStoredPower() {
        return energyStorage.getEnergy();
    }

    private void consumeEnergy(long e) {
        energyStorage.consumeEnergy(e);
    }

    @Override
    protected void tickServer() {
        craftingSystem.tick(level);

        xnetDelay--;
        if (xnetDelay < 0) {
            // If there was no update from XNet for a while then we assume we no longer have information
            xnetAccess = Collections.emptyMap();
            xnetDelay = XNETDELAY;
        }

        if (!items.getStackInSlot(StorageScannerContainer.SLOT_IN).isEmpty()) {
            if (getStoredPower() < StorageScannerConfiguration.rfPerInsert.get()) {
                return;
            }

            ItemStack stack = items.getStackInSlot(StorageScannerContainer.SLOT_IN);
            stack = injectStackInternal(stack, exportToCurrent, this::isInputFromGui);
            items.setStackInSlot(StorageScannerContainer.SLOT_IN, stack);

            consumeEnergy(StorageScannerConfiguration.rfPerInsert.get());
        }
        if (!items.getStackInSlot(StorageScannerContainer.SLOT_IN_AUTO).isEmpty()) {
            if (getStoredPower() < StorageScannerConfiguration.rfPerInsert.get()) {
                return;
            }

            ItemStack stack = items.getStackInSlot(StorageScannerContainer.SLOT_IN_AUTO);
            stack = injectStackInternal(stack, false, this::isInputFromAuto);
            items.setStackInSlot(StorageScannerContainer.SLOT_IN_AUTO, stack);

            consumeEnergy(StorageScannerConfiguration.rfPerInsert.get());
        }
    }

    @Override
    public ItemStack injectStackFromScreen(ItemStack stack, Player player) {
        if (getStoredPower() < StorageScannerConfiguration.rfPerInsert.get()) {
            player.displayClientMessage(ComponentFactory.literal(ChatFormatting.RED + "Not enough power to insert items!"), false);
            return stack;
        }
        if (!checkForRoutableInventories()) {
            player.displayClientMessage(ComponentFactory.literal(ChatFormatting.RED + "There are no routable inventories!"), false);
            return stack;
        }
        stack = injectStackInternal(stack, false, this::isInputFromScreen);
        if (stack.isEmpty()) {
            consumeEnergy(StorageScannerConfiguration.rfPerInsert.get());
            SoundTools.playSound(level, SoundEvents.ITEM_PICKUP, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), 1.0f, 3.0f);
        }
        return stack;
    }

    private boolean checkForRoutableInventories() {
        return inventories.stream()
                .filter(p -> isValid(p) && (!p.equals(getBlockPos()) && isRoutable(p)) && LevelTools.isLoaded(level, p))
                .anyMatch(p -> level.getBlockEntity(p) != null);
    }

    private ItemStack injectStackInternal(ItemStack stack, boolean toSelected, @Nonnull Function<BlockPos, Boolean> testAccess) {
        if (toSelected && lastSelectedInventory != null && lastSelectedInventory.getY() != -1) {
            // Try to insert into the selected inventory
            BlockEntity te = level.getBlockEntity(lastSelectedInventory);
            if (te != null && !(te instanceof StorageScannerTileEntity)) {
                if (testAccess.apply(lastSelectedInventory) && getInputMatcher(lastSelectedInventory).test(stack)) {
                    stack = InventoryTools.insertItem(level, lastSelectedInventory, null, stack);
                    if (stack.isEmpty()) {
                        return stack;
                    }
                }
            }
            return stack;
        }
        final ItemStack finalStack = stack;
        Iterator<BlockEntity> iterator = inventories.stream()
                .filter(p -> testAccess.apply(p) && !p.equals(getBlockPos()) && isRoutable(p) && LevelTools.isLoaded(level, p) && getInputMatcher(p).test(finalStack))
                .map(level::getBlockEntity)
                .filter(te -> te != null && !(te instanceof StorageScannerTileEntity) && !(te instanceof CraftingManagerTileEntity))
                .iterator();
        while (!stack.isEmpty() && iterator.hasNext()) {
            BlockEntity te = iterator.next();
            stack = InventoryTools.insertItem(level, te.getBlockPos(), null, stack);
        }
        return stack;
    }

    /**
     * Give a stack matching the input stack to the player containing either a single
     * item or else a full stack
     *
     * @param stack
     * @param single
     * @param player
     */
    @Override
    public void giveToPlayerFromScreen(ItemStack stack, boolean single, Player player) {
        if (stack.isEmpty()) {
            return;
        }
        if (getStoredPower() < StorageScannerConfiguration.rfPerRequest.get()) {
            player.displayClientMessage(ComponentFactory.literal(ChatFormatting.RED + "Not enough power to request items!"), false);
            return;
        }

        final int[] cnt = {single ? 1 : stack.getMaxStackSize()};
        int orig = cnt[0];
        inventories.stream()
                .filter(this::isOutputFromScreen)
                .map(this::getItemHandlerAt)
                .forEachOrdered(handler -> {
                    handler.ifPresent(h -> {
                        for (int i = 0; i < h.getSlots(); i++) {
                            ItemStack itemStack = h.getStackInSlot(i);
                            if (isItemEqual(stack, itemStack)) {
                                ItemStack received = h.extractItem(i, cnt[0], false);
                                giveItemToPlayer(player, cnt, received);
                            }
                        }
                    });
                });
        if (orig != cnt[0]) {
            consumeEnergy(StorageScannerConfiguration.rfPerRequest.get());
            SoundTools.playSound(level, SoundEvents.ITEM_PICKUP, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), 1.0f, 1.0f);
        }
    }

    private boolean giveItemToPlayer(Player player, int[] cnt, ItemStack received) {
        if (!received.isEmpty() && cnt[0] > 0) {
            cnt[0] -= received.getCount();
            giveToPlayer(received, player);
            return true;
        }
        return false;
    }

    private boolean giveToPlayer(ItemStack stack, Player player) {
        if (stack.isEmpty()) {
            return false;
        }
        if (!player.getInventory().add(stack)) {
            player.spawnAtLocation(stack, 1.05f);
        }
        return true;
    }

    @Override
    public int countItems(Predicate<ItemStack> matcher, boolean starred, @Nullable Integer maxneeded) {
        final int[] cc = {0};
        inventories.stream()
                .filter(p -> isValid(p) && ((!starred) || isRoutable(p)) && LevelTools.isLoaded(level, p))
                .map(level::getBlockEntity)
                .filter(te -> te != null && !(te instanceof StorageScannerTileEntity) && !(te instanceof CraftingManagerTileEntity))
                .allMatch(te -> {
                    InventoryTools.getItems(te, matcher)
                            .forEach(s -> cc[0] += s.getCount());
                    if (maxneeded != null && cc[0] >= maxneeded) {
                        return false;
                    }
                    return true;
                });
        return cc[0];
    }

    @Nonnull
    @Override
    public ItemStack getItem(Predicate<ItemStack> matcher, boolean starred) {
        return inventories.stream()
                .filter(p -> isValid(p) && ((!starred) || isRoutable(p)) && LevelTools.isLoaded(level, p))
                .map(level::getBlockEntity)
                .filter(te -> te != null && !(te instanceof StorageScannerTileEntity) && !(te instanceof CraftingManagerTileEntity))
                .map(te -> InventoryTools.getFirstMatchingItem(te, matcher))
                .filter(s -> !s.isEmpty())
                .findFirst()
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public int countItems(ItemStack match, boolean routable) {
        return countItems(match, routable, null);
    }

    @Override
    public int countItems(ItemStack stack, boolean starred, @Nullable Integer maxneeded) {
        if (stack.isEmpty()) {
            return 0;
        }
        Iterator<BlockEntity> iterator = inventories.stream()
                .filter(p -> isValid(p) && ((!starred) || isRoutable(p)) && LevelTools.isLoaded(level, p))
                .map(level::getBlockEntity)
                .filter(te -> te != null && !(te instanceof StorageScannerTileEntity) && !(te instanceof CraftingManagerTileEntity))
                .iterator();

        int cnt = 0;
        while (iterator.hasNext()) {
            BlockEntity te = iterator.next();
            Integer cachedCount = null;
            if (te instanceof IInventoryTracker tracker) {
                CachedItemCount itemCount = cachedCounts.get(new CachedItemKey(te.getBlockPos(), stack.getItem(), 0 /* @todo 1.14 stack.getMetadata()*/));
                if (itemCount != null) {
                    int oldVersion = itemCount.version();
                    if (oldVersion == tracker.getVersion()) {
                        cachedCount = itemCount.count();
                    }
                }
            }
            if (cachedCount != null) {
                cnt += cachedCount;
            } else {
                final int[] cc = {0};
                InventoryTools.getItems(te, s -> isItemEqual(stack, s))
                        .forEach(s -> cc[0] += s.getCount());
                if (te instanceof IInventoryTracker tracker) {
                    cachedCounts.put(new CachedItemKey(te.getBlockPos(), stack.getItem(), 0 /* @todc 1.14 meta */), new CachedItemCount(tracker.getVersion(), cc[0]));
                }
                cnt += cc[0];
            }
            if (maxneeded != null && cnt >= maxneeded) {
                break;
            }
        }

        return cnt;
    }

    public static boolean isItemEqual(ItemStack thisItem, ItemStack other) {
        if (other.isEmpty()) {
            return false;
        }
        return ItemStack.isSameItem(thisItem, other);
    }


    public Set<BlockPos> performSearch(String search) {

        Predicate<ItemStack> matcher = getMatcher(search);

        Set<BlockPos> output = new HashSet<>();
        inventories.stream()
                .filter(this::isValid)
                .map(level::getBlockEntity)
                .filter(te -> te != null && !(te instanceof StorageScannerTileEntity))
                .forEach(te -> InventoryTools.getItems(te, matcher).forEach(s -> output.add(te.getBlockPos())));
        return output;
    }

    public static Predicate<ItemStack> getMatcher(String search) {
        Predicate<ItemStack> matcher = null;
        search = search.toLowerCase();

        String[] splitted = StringUtils.split(search);
        for (String split : splitted) {
            if (matcher == null) {
                matcher = makeSearchPredicate(split);
            } else {
                matcher = matcher.and(makeSearchPredicate(split));
            }
        }
        if (matcher == null) {
            matcher = s -> true;
        }
        return matcher;
    }

    private static Predicate<ItemStack> makeSearchPredicate(String split) {
        if (split.startsWith("@")) {
            return s -> Tools.getModid(s).toLowerCase().startsWith(split.substring(1));
        } else {
            return s -> s.getHoverName().getString() /* was getFormattedText() */.toLowerCase().contains(split);
        }
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int v) {
        radius = v;
        if (StorageScannerConfiguration.xnetRequired.get() && RFToolsStorage.setup.xnet) {
            radius = 0;
        }
        setChanged();
    }

    public boolean isOpenWideView() {
        return openWideView;
    }

    public void setOpenWideView(boolean openWideView) {
        this.openWideView = openWideView;
        setChanged();
    }

    public boolean isExportToCurrent() {
        return exportToCurrent;
    }

    public void setExportToCurrent(boolean exportToCurrent) {
        this.exportToCurrent = exportToCurrent;
        setChanged();
    }

    private void toggleExportRoutable() {
        exportToCurrent = !exportToCurrent;
        setChanged();
    }

    public boolean isRoutable(BlockPos p) {
        return routable.contains(p);
    }

    public boolean isValid(BlockPos p) {
        if (xnetAccess.containsKey(p)) {
            return true;
        }
        return !inventoriesFromXNet.contains(p);
    }

    public boolean isOutputFromGui(BlockPos p) {
        InventoryAccessSettings settings = xnetAccess.get(p);
        if (settings != null) {
            return !settings.isBlockOutputGui();
        }
        return !inventoriesFromXNet.contains(p);
    }

    public boolean isOutputFromScreen(BlockPos p) {
        InventoryAccessSettings settings = xnetAccess.get(p);
        if (settings != null) {
            return !settings.isBlockOutputScreen();
        }
        return !inventoriesFromXNet.contains(p);
    }

    public boolean isOutputFromAuto(BlockPos p) {
        InventoryAccessSettings settings = xnetAccess.get(p);
        if (settings != null) {
            return !settings.isBlockOutputAuto();
        }
        return !inventoriesFromXNet.contains(p);
    }

    public Predicate<ItemStack> getInputMatcher(BlockPos p) {
        InventoryAccessSettings settings = xnetAccess.get(p);
        if (settings != null) {
            return settings.getMatcher();
        }
        return stack -> true;
    }

    public boolean isInputFromGui(BlockPos p) {
        InventoryAccessSettings settings = xnetAccess.get(p);
        if (settings != null) {
            return !settings.isBlockInputGui();
        }
        return !inventoriesFromXNet.contains(p);
    }

    public boolean isInputFromScreen(BlockPos p) {
        InventoryAccessSettings settings = xnetAccess.get(p);
        if (settings != null) {
            return !settings.isBlockInputScreen();
        }
        return !inventoriesFromXNet.contains(p);
    }

    public boolean isInputFromAuto(BlockPos p) {
        InventoryAccessSettings settings = xnetAccess.get(p);
        if (settings != null) {
            return !settings.isBlockInputAuto();
        }
        return !inventoriesFromXNet.contains(p);
    }

    public void toggleRoutable(BlockPos p) {
        if (routable.contains(p)) {
            routable.remove(p);
        } else {
            routable.add(p);
        }
        setChanged();
    }

    public void register(Map<BlockPos, InventoryAccessSettings> access) {
        xnetAccess = access;
        xnetDelay = XNETDELAY;
    }

    private void moveUp(int index) {
        if (index <= 0) {
            return;
        }
        if (index >= inventories.size()) {
            return;
        }
        BlockPos p1 = inventories.get(index - 1);
        BlockPos p2 = inventories.get(index);
        inventories.set(index - 1, p2);
        inventories.set(index, p1);
        setChanged();
    }

    private void moveTop(int index) {
        if (index <= 0) {
            return;
        }
        if (index >= inventories.size()) {
            return;
        }
        BlockPos p = inventories.get(index);
        inventories.remove(index);
        inventories.add(0, p);
        setChanged();
    }

    private void moveDown(int index) {
        if (index < 0) {
            return;
        }
        if (index >= inventories.size() - 1) {
            return;
        }
        BlockPos p1 = inventories.get(index);
        BlockPos p2 = inventories.get(index + 1);
        inventories.set(index, p2);
        inventories.set(index + 1, p1);
        setChanged();
    }

    private void moveBottom(int index) {
        if (index < 0) {
            return;
        }
        if (index >= inventories.size() - 1) {
            return;
        }
        BlockPos p = inventories.get(index);
        inventories.remove(index);
        inventories.add(p);
        setChanged();
    }

    private void removeInventory(int index) {
        if (index < 0) {
            return;
        }
        if (index >= inventories.size()) {
            return;
        }
        BlockPos p = inventories.get(index);
        if (inventoriesFromXNet.contains(p)) {
            // Cannot remove inventories from xnet
            return;
        }
        BlockPos removed = inventories.remove(index);
        if (craftingInventories != null) {
            craftingInventories.remove(removed);
        }
        setChanged();
    }

    private boolean canPlayerAccess(Player fakePlayer, BlockPos p) {
        if (StorageScannerConfiguration.scannerNoRestrictions.get()) {
            return true;
        }
        return level.getBlockState(p).canEntityDestroy(level, p, fakePlayer);
    }

    @Override
    public void clearCachedCounts() {
        cachedCounts.clear();
    }

    public Stream<BlockPos> findInventories() {
        if (RFToolsStorage.setup.xnet && StorageScannerConfiguration.xnetRequired.get()) {
            radius = 0;
        }

        // Clear the caches
        cachedCounts.clear();
        inventoriesFromXNet.clear();

        // First remove all inventories that are either out of range or no longer an inventory:
        List<BlockPos> old = inventories;
        Set<BlockPos> oldAdded = new HashSet<>();
        Set<BlockPos> seenPositions = new HashSet<>();
        inventories = new ArrayList<>();
        craftingInventories = new ArrayList<>();

        Player fakePlayer = lazyPlayer.get();

        for (BlockPos p : old) {
            if (xnetAccess.containsKey(p) || inRange(p)) {
                BlockEntity te = level.getBlockEntity(p);
                if (te != null && !(te instanceof StorageScannerTileEntity)) {
                    if (canPlayerAccess(fakePlayer, p)) {
                        if (te instanceof CraftingManagerTileEntity) {
                            if (seenPositions.add(p)) {
                                inventories.add(p);
                                craftingInventories.add(p);
                                oldAdded.add(p);
                            }
                        } else {
                            te.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
                                if (seenPositions.add(p)) {
                                    inventories.add(p);
                                    oldAdded.add(p);
                                }
                            });
                        }
                    }
                }
            }
        }

        // Now append all inventories that are new.
        for (int x = getBlockPos().getX() - radius; x <= getBlockPos().getX() + radius; x++) {
            for (int z = getBlockPos().getZ() - radius; z <= getBlockPos().getZ() + radius; z++) {
                for (int y = getBlockPos().getY() - radius; y <= getBlockPos().getY() + radius; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    inventoryAddNew(oldAdded, seenPositions, p);
                }
            }
        }
        // @todo xnet support for crafting manager
        for (BlockPos p : xnetAccess.keySet()) {
            inventoryAddNew(oldAdded, seenPositions, p);
            inventoriesFromXNet.add(p);
        }

        return getAllInventories();
    }

    public List<BlockPos> getCraftingInventories() {
        if (craftingInventories == null) {
            craftingInventories = new ArrayList<>();
            getAllInventories().forEach(pos -> {
                if (level.getBlockEntity(pos) instanceof CraftingManagerTileEntity) {
                    craftingInventories.add(pos);
                }
            });
        }
        return craftingInventories;
    }

    public Stream<BlockPos> getAllInventories() {
        return inventories.stream()
                .filter(this::isValid);
    }

    private void inventoryAddNew(Set<BlockPos> oldAdded,
                                 Set<BlockPos> seenPositions, BlockPos p) {
        if (!oldAdded.contains(p)) {
            BlockEntity te = level.getBlockEntity(p);
            if (canPlayerAccess(lazyPlayer.get(), p)) {
                if (te != null && !(te instanceof StorageScannerTileEntity)) {
                    if (te instanceof CraftingManagerTileEntity) {
                        if (seenPositions.add(p)) {
                            inventories.add(p);
                            craftingInventories.add(p);
                        }
                    } else if (!inventories.contains(p)) {
                        te.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
                            if (seenPositions.add(p)) {
                                inventories.add(p);
                            }
                        });
                    }
                }
            }
        }
    }

    private boolean inRange(BlockPos p) {
        return p.getX() >= getBlockPos().getX() - radius && p.getX() <= getBlockPos().getX() + radius && p.getY() >= getBlockPos().getY() - radius && p.getY() <= getBlockPos().getY() + radius && p.getZ() >= getBlockPos().getZ() - radius && p.getZ() <= getBlockPos().getZ() + radius;
    }

    private static final ItemStack DUMMY = new ItemStack(Items.BEDROCK, 666);

    @Override
    public ItemStack requestItem(Predicate<ItemStack> matcher, boolean simulate, int amount, boolean doRoutable) {
        if (getStoredPower() < StorageScannerConfiguration.rfPerRequest.get()) {
            return ItemStack.EMPTY;
        }
        Player fakePlayer = lazyPlayer.get();
        return inventories.stream()
                .filter(p -> isOutputFromAuto(p) && ((!doRoutable) || isRoutable(p)))
                .filter(p -> !(level.getBlockEntity(p) instanceof CraftingManagerTileEntity))
                .filter(p -> canPlayerAccess(fakePlayer, p))
                .map(this::getItemHandlerAt)
                .map(handler -> handler.map(h -> {
                    for (int i = 0; i < h.getSlots(); i++) {
                        ItemStack itemStack = h.getStackInSlot(i);
                        if (matcher.test(itemStack)) {
                            ItemStack received = h.extractItem(i, amount, simulate);
                            if (!received.isEmpty()) {
                                return received.copy();
                            }
                        }
                    }
                    return DUMMY;
                }).orElse(DUMMY))
                .filter(s -> s != DUMMY)
                .findFirst()
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack requestItem(ItemStack match, int amount, boolean doRoutable) {
        if (match.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (getStoredPower() < StorageScannerConfiguration.rfPerRequest.get()) {
            return ItemStack.EMPTY;
        }

        final ItemStack[] result = {ItemStack.EMPTY};
        final int[] cnt = {Math.min(match.getMaxStackSize(), amount)};
        Player fakePlayer = lazyPlayer.get();
        inventories.stream()
                .filter(p -> isOutputFromAuto(p) && (!doRoutable) || isRoutable(p))
                .filter(p -> !(level.getBlockEntity(p) instanceof CraftingManagerTileEntity))
                .filter(p -> canPlayerAccess(fakePlayer, p))
                .map(this::getItemHandlerAt)
                .allMatch(handler -> {
                    handler.ifPresent(h -> {
                        for (int i = 0; i < h.getSlots(); i++) {
                            ItemStack itemStack = h.getStackInSlot(i);
                            if (isItemEqual(match, itemStack)) {
                                ItemStack received = h.extractItem(i, cnt[0], false);
                                if (!received.isEmpty()) {
                                    if (result[0].isEmpty()) {
                                        result[0] = received;
                                    } else {
                                        result[0].grow(received.getCount());
                                    }
                                    cnt[0] -= received.getCount();
                                }
                            }
                        }
                    });
                    return cnt[0] > 0;
                });
        if (!result[0].isEmpty()) {
            consumeEnergy(StorageScannerConfiguration.rfPerRequest.get());
        }
        return result[0];
    }

    @Nonnull
    private LazyOptional<IItemHandler> getItemHandlerAt(BlockPos p) {
        if (!LevelTools.isLoaded(level, p)) {
            return LazyOptional.empty();
        }
        BlockEntity te = level.getBlockEntity(p);
        if (te == null || te instanceof StorageScannerTileEntity) {
            return LazyOptional.empty();
        }
        return getItemHandlerAt(te, null);
    }

    // @todo move to McJtyLib
    @Nonnull
    private static LazyOptional<IItemHandler> getItemHandlerAt(@Nullable BlockEntity te, Direction intSide) {
        if (te != null) {
            return te.getCapability(ForgeCapabilities.ITEM_HANDLER, intSide);
        } else {
            return LazyOptional.empty();
        }
    }

    @Override
    public int insertItem(ItemStack stack) {
        ItemStack s = insertItem(stack, false);
        return s.getCount();
    }

    @Override
    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        if (getStoredPower() < StorageScannerConfiguration.rfPerInsert.get()) {
            return stack;
        }

        ItemStack result = insertInternal(stack, simulate);

        consumeEnergy(StorageScannerConfiguration.rfPerInsert.get());
        return result;
    }

    /// Insert an item without using power
    public ItemStack insertInternal(ItemStack stack, boolean simulate) {
        final ItemStack[] toInsert = {stack.copy()};

        Player fakePlayer = lazyPlayer.get();
        Iterator<LazyOptional<IItemHandler>> iterator = inventories.stream()
                .filter(p -> isInputFromAuto(p) && (!p.equals(getBlockPos()) && isRoutable(p) && getInputMatcher(p).test(stack)))
                .filter(p -> !(level.getBlockEntity(p) instanceof CraftingManagerTileEntity))
                .filter(p -> canPlayerAccess(fakePlayer, p))
                .map(this::getItemHandlerAt)
                .filter(LazyOptional::isPresent)
                .iterator();

        while (!toInsert[0].isEmpty() && iterator.hasNext()) {
            LazyOptional<IItemHandler> handler = iterator.next();
            handler.ifPresent(h -> {
                toInsert[0] = ItemHandlerHelper.insertItem(h, toInsert[0], simulate);
            });
        }
        return toInsert[0];
    }

    private ItemStack requestStackFromInv(BlockPos invPos, ItemStack requested, Integer[] todo, ItemStack outSlot) {
        BlockEntity tileEntity = level.getBlockEntity(invPos);
        if (tileEntity instanceof StorageScannerTileEntity) {
            return outSlot;
        }

        int size = InventoryTools.getInventorySize(tileEntity);

        for (int i = 0; i < size; i++) {
            ItemStack stack = ItemStackTools.getStack(tileEntity, i);
            if (ItemHandlerHelper.canItemStacksStack(requested, stack)) {
                ItemStack extracted = ItemStackTools.extractItem(tileEntity, i, todo[0]);
                todo[0] -= extracted.getCount();
                if (outSlot.isEmpty()) {
                    outSlot = extracted;
                } else {
                    outSlot.grow(extracted.getCount());
                }
                if (todo[0] == 0) {
                    break;
                }
            }
        }
        return outSlot;
    }

    /**
     * Called from the crafting manager. There are three possible results:
     * - Returns null: the ingredients are not available and there are no crafters able to make them
     * - Returns empty list: the ingredients are not available but there are crafters that are able to make
     * them and requestCraft consumers will be fired for all missing items
     * - Returns a list of itemstacks as extracted from the storage scanner. The craft can go on
     * <p>
     * If the extract is true the requested items are actually extracted in case they are available. Otherwise
     * nothing happens (and an empty list will be returned or null)
     */
    @Nullable
    public List<ItemStack> requestIngredients(List<Ingredient> ingredients, Consumer<Ingredient> missingIngredientConsumer, boolean extract) {
        List<Ingredient> missing = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            ItemStack stack = requestItem(ingredient, true, 1, true);
            // If the stack is empty but it matches the ingredient it is not considered a failure
            if (stack.isEmpty() && !ingredient.test(stack)) {
                missing.add(ingredient);
            }
        }
        if (missing.isEmpty()) {
            // Nothing is missing. Really extract
            if (extract) {
                List<ItemStack> stacks = new ArrayList<>();
                for (Ingredient ingredient : ingredients) {
                    ItemStack stack = requestItem(ingredient, false, 1, true);
                    stacks.add(stack);
                }
                return stacks;
            } else {
                return Collections.emptyList();
            }
        } else {
            // There are missing items. Check if there are recipes for them
            for (Ingredient ingredient : missing) {
                if (getAllInventories().noneMatch(p -> {
                    BlockEntity te = level.getBlockEntity(p);
                    if (te instanceof CraftingManagerTileEntity craftingManager) {
                        if (craftingManager.canCraft(ingredient)) {
                            // The crafting manager can craft this item
                            missingIngredientConsumer.accept(ingredient);
                            return true;
                        }
                    }
                    return false;
                })) {
                    return null;    // No recipe exists for this ingredient
                }
            }
            // All ingredients are there or have recipes
            return Collections.emptyList();
        }
    }


    // Meant to be used from the gui
    public void requestCraft(BlockPos invPos, ItemStack requested, int amount, Player player) {
        int rf = StorageScannerConfiguration.rfPerRequest.get();
        if (amount >= 0) {
            rf /= 10;       // Less RF usage for requesting less items
        }
        if (amount == -1) {
            amount = requested.getMaxStackSize();
        }
        if (getStoredPower() < rf) {
            return;
        }

        craftingSystem.requestCraft(requested, amount);
    }

    // Meant to be used from the gui
    public void requestStack(BlockPos invPos, ItemStack requested, int amount, Player player) {
        int rf = StorageScannerConfiguration.rfPerRequest.get();
        if (amount >= 0) {
            rf /= 10;       // Less RF usage for requesting less items
        }
        if (amount == -1) {
            amount = requested.getMaxStackSize();
        }
        if (getStoredPower() < rf) {
            return;
        }

        Integer[] todo = new Integer[]{amount};

        int finalAmount = amount;
        int finalRf = rf;

        ItemStack outSlot = items.getStackInSlot(StorageScannerContainer.SLOT_OUT);
        if (!outSlot.isEmpty()) {
            // Check if the items are the same and there is room
            if (!ItemHandlerHelper.canItemStacksStack(outSlot, requested)) {
                return;
            }
            if (outSlot.getCount() >= requested.getMaxStackSize()) {
                return;
            }
            todo[0] = Math.min(todo[0], requested.getMaxStackSize() - outSlot.getCount());
        }

        if (invPos.getY() == -1) {
            Player fakePlayer = lazyPlayer.get();
            Iterator<BlockPos> iterator = inventories.stream()
                    .filter(p -> !(level.getBlockEntity(p) instanceof CraftingManagerTileEntity))
                    .filter(p -> canPlayerAccess(fakePlayer, p))
                    .filter(p -> isOutputFromGui(p) && isRoutable(p))
                    .iterator();
            while (iterator.hasNext()) {
                BlockPos blockPos = iterator.next();
                outSlot = requestStackFromInv(blockPos, requested, todo, outSlot);
                if (todo[0] == 0) {
                    break;
                }

            }
        } else {
            if (isOutputFromGui(invPos)) {
                outSlot = requestStackFromInv(invPos, requested, todo, outSlot);
            }
        }

        if (todo[0] == finalAmount) {
            // Nothing happened
            return;
        }

        consumeEnergy(finalRf);
        items.setStackInSlot(StorageScannerContainer.SLOT_OUT, outSlot);

        if (StorageScannerConfiguration.requestStraightToInventory.get()) {
            if (player.getInventory().add(outSlot)) {
                items.setStackInSlot(StorageScannerContainer.SLOT_OUT, ItemStack.EMPTY);
            }
        }
    }

    private void addItemStack(List<ItemStack> stacks, Set<Item> foundItems, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (foundItems.contains(stack.getItem())) {
            for (ItemStack s : stacks) {
                if (ItemHandlerHelper.canItemStacksStack(s, stack)) {
                    s.grow(stack.getCount());
                    return;
                }
            }
        }
        // If we come here we need to append a new stack
        stacks.add(stack.copy());
        foundItems.add(stack.getItem());
    }


    public void getInventoryForBlock(BlockPos cpos, List<ItemStack> stacks, List<ItemStack> craftable) {
        Set<Item> foundItems = new HashSet<>();

        lastSelectedInventory = cpos;

        if (cpos.getY() == -1) {
            // Get all starred inventories
            for (BlockPos blockPos : inventories) {
                if (routable.contains(blockPos)) {
                    addItemsFromInventory(blockPos, foundItems, stacks, craftable);
                }
            }
        } else {
            addItemsFromInventory(cpos, foundItems, stacks, craftable);
        }
    }

    private void addItemsFromInventory(BlockPos cpos, Set<Item> foundItems, List<ItemStack> stacks, List<ItemStack> craftable) {
        BlockEntity tileEntity = level.getBlockEntity(cpos);
        if (tileEntity instanceof CraftingManagerTileEntity) {
            for (ItemStack stack : ((CraftingManagerTileEntity) tileEntity).getCraftables()) {
                addItemStack(craftable, foundItems, stack);
            }
        } else {
            getItemHandlerAt(tileEntity, null).ifPresent(h -> {
                for (int i = 0; i < h.getSlots(); i++) {
                    addItemStack(stacks, foundItems, h.getStackInSlot(i));
                }
            });
        }
    }

    public SortingMode getSortMode() {
        return sortMode;
    }

    public void setSortMode(SortingMode sortMode) {
        this.sortMode = sortMode;
        setChanged();
    }

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        craftingSystem.read(tagCompound.getCompound("CS"));
        ListTag list = tagCompound.getList("inventories", Tag.TAG_COMPOUND);
        inventories.clear();
        craftingInventories = null;
        for (Tag inbt : list) {
            CompoundTag tag = (CompoundTag) inbt;
            BlockPos c = BlockPosTools.read(tag, "c");
            inventories.add(c);
        }
        list = tagCompound.getList("routable", Tag.TAG_COMPOUND);
        routable.clear();
        for (Tag inbt : list) {
            CompoundTag tag = (CompoundTag) inbt;
            BlockPos c = BlockPosTools.read(tag, "c");
            routable.add(c);
        }
        list = tagCompound.getList("fromxnet", Tag.TAG_COMPOUND);
        inventoriesFromXNet.clear();
        for (Tag inbt : list) {
            CompoundTag tag = (CompoundTag) inbt;
            BlockPos c = BlockPosTools.read(tag, "c");
            inventoriesFromXNet.add(c);
        }
    }

    @Override
    protected void loadInfo(CompoundTag tagCompound) {
        super.loadInfo(tagCompound);
        if (tagCompound.contains("Info")) {
            CompoundTag infoTag = tagCompound.getCompound("Info");
            if (infoTag.contains("radius")) {
                radius = infoTag.getInt("radius");
            }
            if (infoTag.contains("exportC")) {
                exportToCurrent = infoTag.getBoolean("exportC");
            }
            if (infoTag.contains("wideview")) {
                openWideView = infoTag.getBoolean("wideview");
            }
            if (infoTag.contains("grid")) {
                craftingGrid.readFromNBT(infoTag.getCompound("grid"));
            }
            if (infoTag.contains("sortMode")) {
                int m = infoTag.getInt("sortMode");
                sortMode = SortingMode.values()[m];
            }
        } else {
            openWideView = true;
            sortMode = SortingMode.NAME;
        }
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tagCompound) {
        super.saveAdditional(tagCompound);
        tagCompound.put("CS", craftingSystem.write());
        ListTag list = new ListTag();
        for (BlockPos c : inventories) {
            CompoundTag tag = BlockPosTools.write(c);
            list.add(tag);
        }
        tagCompound.put("inventories", list);
        list = new ListTag();
        for (BlockPos c : routable) {
            CompoundTag tag = BlockPosTools.write(c);
            list.add(tag);
        }
        tagCompound.put("routable", list);
        list = new ListTag();
        for (BlockPos c : inventoriesFromXNet) {
            CompoundTag tag = BlockPosTools.write(c);
            list.add(tag);
        }
        tagCompound.put("fromxnet", list);
    }

    @Override
    protected void saveInfo(CompoundTag tagCompound) {
        super.saveInfo(tagCompound);
        CompoundTag infoTag = getOrCreateInfo(tagCompound);
        infoTag.putInt("radius", radius);
        infoTag.putBoolean("exportC", exportToCurrent);
        infoTag.putBoolean("wideview", openWideView);
        infoTag.put("grid", craftingGrid.writeToNBT());
        infoTag.putInt("sortMode", sortMode.ordinal());
    }


    @ServerCommand
    public static final Command<?> CMD_CLEARGRID = Command.<StorageScannerTileEntity>create("clearGrid", (te, player, params) -> te.clearGrid());

    private void clearGrid() {
        CraftingGridInventory inventory = craftingGrid.getCraftingGridInventory();
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
        setChanged();
    }

    @ServerCommand
    public static final Command<?> CMD_UP = Command.<StorageScannerTileEntity>create("scanner.up",
            (te, player, params) -> te.moveUp(params.get(PARAM_INDEX)));
    @ServerCommand
    public static final Command<?> CMD_TOP = Command.<StorageScannerTileEntity>create("scanner.top",
            (te, player, params) -> te.moveTop(params.get(PARAM_INDEX)));
    @ServerCommand
    public static final Command<?> CMD_DOWN = Command.<StorageScannerTileEntity>create("scanner.down",
            (te, player, params) -> te.moveDown(params.get(PARAM_INDEX)));
    @ServerCommand
    public static final Command<?> CMD_BOTTOM = Command.<StorageScannerTileEntity>create("scanner.bottom",
            (te, player, params) -> te.moveBottom(params.get(PARAM_INDEX)));
    @ServerCommand
    public static final Command<?> CMD_REMOVE = Command.<StorageScannerTileEntity>create("scanner.remove",
            (te, player, params) -> te.removeInventory(params.get(PARAM_INDEX)));
    @ServerCommand
    public static final Command<?> CMD_TOGGLEROUTABLE = Command.<StorageScannerTileEntity>create("scanner.toggleRoutable",
            (te, player, params) -> te.toggleRoutable(params.get(PARAM_POS)));
    @ServerCommand
    public static final Command<?> CMD_SETVIEW = Command.<StorageScannerTileEntity>create("scanner.setView",
            (te, player, params) -> te.setOpenWideView(params.get(PARAM_VIEW)));

    public static final Key<Long> PARAM_ENERGY = new Key<>("energy", Type.LONG);
    public static final Key<Boolean> PARAM_EXPORT = new Key<>("export", Type.BOOLEAN);
    @ServerCommand
    public static final ResultCommand<?> CMD_SCANNER_INFO = ResultCommand.<StorageScannerTileEntity>create("getScannerInfo",
            (te, player, params) -> TypedMap.builder()
                    .put(PARAM_ENERGY, te.getStoredPower())
                    .put(PARAM_EXPORT, te.isExportToCurrent())
                    .build(),
            (te, player, params) -> {
                te.rfReceived = params.get(PARAM_ENERGY);
                te.exportToCurrentReceived = params.get(PARAM_EXPORT);
            });

    /**
     * Return true if this is a dummy TE. i.e. this happens only when accessing a
     * storage scanner in a tablet on the client side.
     */
    public boolean isDummy() {
        return dummyType != null;
    }

    @Override
    public ResourceKey<Level> getDimension() {
        if (dummyType != null) {
            return dummyType;
        }
        return super.getDimension();
    }

    /**
     * This is used client side only for the GUI.
     * Return the position of the crafting grid container. This
     * is either the position of this tile entity (in case we are just looking
     * directly at the storage scanner), the position of a 'watching' tile
     * entity (in case we are a dummy for the storage terminal) or else null
     * in case we're using a handheld item.
     */
    public BlockPos getCraftingGridContainerPos() {
        return getBlockPos();
    }

    /**
     * This is used client side only for the GUI.
     * Get the real crafting grid provider
     */
    public CraftingGridProvider getCraftingGridProvider() {
        return this;
    }

    /**
     * This is used client side only for the GUI.
     * Return the position of the actual storage scanner
     */
    public BlockPos getStorageScannerPos() {
        return getBlockPos();
    }

    public CraftingSystem getCraftingSystem() {
        return craftingSystem;
    }

    @Nonnull
    private IInformationScreenInfo createScreenInfo() {
        return new StorageScannerInformationScreenInfo(this);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        if (cap == CapabilityInformationScreenInfo.INFORMATION_SCREEN_INFO_CAPABILITY) {
            return infoScreenInfo.cast();
        }
        return super.getCapability(cap, facing);
    }

}
