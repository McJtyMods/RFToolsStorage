package mcjty.rftoolsstorage.modules.scanner.blocks;

import com.google.common.base.Function;
import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.api.infusable.CapabilityInfusable;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.bindings.DefaultAction;
import mcjty.lib.bindings.DefaultValue;
import mcjty.lib.bindings.IAction;
import mcjty.lib.bindings.IValue;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.GenericTileEntity;
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
import mcjty.rftoolsstorage.modules.scanner.StorageScannerSetup;
import mcjty.rftoolsstorage.modules.scanner.tools.CachedItemCount;
import mcjty.rftoolsstorage.modules.scanner.tools.CachedItemKey;
import mcjty.rftoolsstorage.modules.scanner.tools.InventoryAccessSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerContainer.CONTAINER_FACTORY;

public class StorageScannerTileEntity extends GenericTileEntity implements ITickableTileEntity,
        CraftingGridProvider, JEIRecipeAcceptor, IStorageScanner {

    public static final String CMD_SCANNER_INFO = "getScannerInfo";
    public static final Key<Long> PARAM_ENERGY = new Key<>("energy", Type.LONG);
    public static final Key<Boolean> PARAM_EXPORT = new Key<>("export", Type.BOOLEAN);

    public static final String CMD_UP = "scanner.up";
    public static final String CMD_TOP = "scanner.top";
    public static final String CMD_DOWN = "scanner.down";
    public static final String CMD_BOTTOM = "scanner.bottom";
    public static final String CMD_REMOVE = "scanner.remove";
    public static final String CMD_TOGGLEROUTABLE = "scanner.toggleRoutable";
    public static final String CMD_SETVIEW = "scanner.setView";

    public static final Key<Integer> PARAM_INDEX = new Key<>("index", Type.INTEGER);
    public static final Key<BlockPos> PARAM_POS = new Key<>("pos", Type.BLOCKPOS);
    public static final Key<Boolean> PARAM_VIEW = new Key<>("view", Type.BOOLEAN);

    public static final String ACTION_CLEARGRID = "clearGrid";

    public static final Key<Boolean> VALUE_EXPORT = new Key<>("export", Type.BOOLEAN);
    public static final Key<Integer> VALUE_RADIUS = new Key<>("radius", Type.INTEGER);

    // Client side data returned by CMD_SCANNER_INFO
    public static long rfReceived = 0;
    public static boolean exportToCurrentReceived = false;

    @Override
    public IAction[] getActions() {
        return new IAction[]{
                new DefaultAction(ACTION_CLEARGRID, this::clearGrid),
        };
    }

    @Override
    public IValue<?>[] getValues() {
        return new IValue[]{
                new DefaultValue<>(VALUE_EXPORT, this::isExportToCurrent, this::setExportToCurrent),
                new DefaultValue<>(VALUE_RADIUS, this::getRadius, this::setRadius),
        };
    }

    public static final int XNETDELAY = 40;

    private CraftingSystem craftingSystem = new CraftingSystem(this);
    private List<BlockPos> inventories = new ArrayList<>();
    private List<BlockPos> craftingInventories = null; // Subset of 'inventories' with all the crafting managers
    private Set<BlockPos> inventoriesFromXNet = new HashSet<>();

    // This data is fed directly by the storage channel system (XNet) and is
    // cleared automatically if that system stops or is disabled
    private Map<BlockPos, InventoryAccessSettings> xnetAccess = Collections.emptyMap();
    private int xnetDelay = XNETDELAY;      // Timer to control when to clear the above

    private Map<CachedItemKey, CachedItemCount> cachedCounts = new HashMap<>();
    private Set<BlockPos> routable = new HashSet<>();
    private int radius = 1;

    // This is set on a client-side dummy tile entity for a tablet
    private DimensionType monitorDim;

    private boolean exportToCurrent = false;
    private BlockPos lastSelectedInventory = null;

    // Indicates if for this storage scanner the inventories should be shown wide
    private boolean openWideView = true;

    private LazyOptional<IInformationScreenInfo> infoScreenInfo = LazyOptional.of(this::createScreenInfo);
    private LazyOptional<GenericEnergyStorage> energyHandler = LazyOptional.of(() -> new GenericEnergyStorage(this, true, StorageScannerConfiguration.MAXENERGY.get(), StorageScannerConfiguration.RECEIVEPERTICK.get()));
    private LazyOptional<NoDirectionItemHander> itemHandler = LazyOptional.of(this::createItemHandler);
    private LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<StorageScannerContainer>("Storage Scanner")
            .containerSupplier((windowId,player) -> new StorageScannerContainer(windowId, getPos(), player, StorageScannerTileEntity.this))
            .itemHandler(() -> getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(h -> h).orElseThrow(RuntimeException::new)));
    private LazyOptional<IInfusable> infusableHandler = LazyOptional.of(() -> new DefaultInfusable(StorageScannerTileEntity.this));

    private CraftingGrid craftingGrid = new CraftingGrid();

    public StorageScannerTileEntity() {
        super(StorageScannerSetup.TYPE_STORAGE_SCANNER.get());
        monitorDim = null;
        radius = (StorageScannerConfiguration.xnetRequired.get() && RFToolsStorage.setup.xnet) ? 0 : 1;
    }

    //    public StorageScannerTileEntity() {
//        super(StorageScannerConfiguration.MAXENERGY.get(), StorageScannerConfiguration.RECEIVEPERTICK.get());
//        monitorDim = null;
//        radius = (StorageScannerConfiguration.xnetRequired.get() && RFTools.setup.xnet) ? 0 : 1;
//    }

    // This constructor is used for constructing a dummy client-side tile entity when
    // accessing the storage scanner remotely
//    public StorageScannerTileEntity(PlayerEntity PlayerEntity, int monitordim) {
//        super(StorageScannerConfiguration.MAXENERGY.get(), StorageScannerConfiguration.RECEIVEPERTICK.get());
//        this.monitorDim = monitordim;
//    }

    @Override
    public void storeRecipe(int index) {
        getCraftingGrid().storeRecipe(index);
    }

    @Override
    public void setRecipe(int index, ItemStack[] stacks) {
        craftingGrid.setRecipe(index, stacks);
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
        CraftingRecipe activeRecipe = craftingGrid.getActiveRecipe();
        return craft(player, n, test, activeRecipe);
    }

    @Nonnull
    public int[] craft(PlayerEntity player, int n, boolean test, CraftingRecipe activeRecipe) {
        TileEntityItemSource itemSource = new TileEntityItemSource()
                .add(new InvWrapper(player.inventory), 0);
        inventories.stream()
                .filter(p -> isOutputFromGui(p) && isRoutable(p))
                .forEachOrdered(p -> {
                    TileEntity tileEntity = world.getTileEntity(p);
                    if (tileEntity instanceof CraftingManagerTileEntity) {
                        // @todo crafting manager
                    } else if (!(tileEntity instanceof StorageScannerTileEntity)) {
                        itemSource.add(tileEntity, 0);
                    }
                });

        if (test) {
            return StorageCraftingTools.testCraftItems(player, n, activeRecipe, itemSource);
        } else {
            StorageCraftingTools.craftItems(player, n, activeRecipe, itemSource);
            return new int[0];
        }
    }

    @Override
    public void setGridContents(List<ItemStack> stacks) {
        for (int i = 0; i < stacks.size(); i++) {
            craftingGrid.getCraftingGridInventory().setStackInSlot(i, stacks.get(i));
        }
        markDirty();
    }

    private long getStoredPower() {
        return energyHandler.map(h -> h.getEnergy()).orElse(0L);
    }

    private void consumeEnergy(long e) {
        energyHandler.ifPresent(h -> h.consumeEnergy(e));
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            craftingSystem.tick(world);

            xnetDelay--;
            if (xnetDelay < 0) {
                // If there was no update from XNet for a while then we assume we no longer have information
                xnetAccess = Collections.emptyMap();
                xnetDelay = XNETDELAY;
            }

            itemHandler.ifPresent(h -> {
                if (!h.getStackInSlot(StorageScannerContainer.SLOT_IN).isEmpty()) {
                    if (getStoredPower() < StorageScannerConfiguration.rfPerInsert.get()) {
                        return;
                    }

                    ItemStack stack = h.getStackInSlot(StorageScannerContainer.SLOT_IN);
                    stack = injectStackInternal(stack, exportToCurrent, this::isInputFromGui);
                    h.setStackInSlot(StorageScannerContainer.SLOT_IN, stack);

                    consumeEnergy(StorageScannerConfiguration.rfPerInsert.get());
                }
                if (!h.getStackInSlot(StorageScannerContainer.SLOT_IN_AUTO).isEmpty()) {
                    if (getStoredPower() < StorageScannerConfiguration.rfPerInsert.get()) {
                        return;
                    }

                    ItemStack stack = h.getStackInSlot(StorageScannerContainer.SLOT_IN_AUTO);
                    stack = injectStackInternal(stack, false, this::isInputFromAuto);
                    h.setStackInSlot(StorageScannerContainer.SLOT_IN_AUTO, stack);

                    consumeEnergy(StorageScannerConfiguration.rfPerInsert.get());
                }
            });
        }
    }


    @Override
    public ItemStack injectStackFromScreen(ItemStack stack, PlayerEntity player) {
        if (getStoredPower() < StorageScannerConfiguration.rfPerInsert.get()) {
            player.sendStatusMessage(new StringTextComponent(TextFormatting.RED + "Not enough power to insert items!"), false);
            return stack;
        }
        if (!checkForRoutableInventories()) {
            player.sendStatusMessage(new StringTextComponent(TextFormatting.RED + "There are no routable inventories!"), false);
            return stack;
        }
        stack = injectStackInternal(stack, false, this::isInputFromScreen);
        if (stack.isEmpty()) {
            consumeEnergy(StorageScannerConfiguration.rfPerInsert.get());
            SoundTools.playSound(world, SoundEvents.ENTITY_ITEM_PICKUP, getPos().getX(), getPos().getY(), getPos().getZ(), 1.0f, 3.0f);
        }
        return stack;
    }

    private boolean checkForRoutableInventories() {
        return inventories.stream()
                .filter(p -> isValid(p) && (!p.equals(getPos()) && isRoutable(p)) && WorldTools.chunkLoaded(world, p))
                .anyMatch(p -> world.getTileEntity(p) != null);
    }

    private ItemStack injectStackInternal(ItemStack stack, boolean toSelected, @Nonnull Function<BlockPos, Boolean> testAccess) {
        if (toSelected && lastSelectedInventory != null && lastSelectedInventory.getY() != -1) {
            // Try to insert into the selected inventory
            TileEntity te = world.getTileEntity(lastSelectedInventory);
            if (te != null && !(te instanceof StorageScannerTileEntity)) {
                if (testAccess.apply(lastSelectedInventory) && getInputMatcher(lastSelectedInventory).test(stack)) {
                    stack = InventoryHelper.insertItem(world, lastSelectedInventory, null, stack);
                    if (stack.isEmpty()) {
                        return stack;
                    }
                }
            }
            return stack;
        }
        final ItemStack finalStack = stack;
        Iterator<TileEntity> iterator = inventories.stream()
                .filter(p -> testAccess.apply(p) && !p.equals(getPos()) && isRoutable(p) && WorldTools.chunkLoaded(world, p) && getInputMatcher(p).test(finalStack))
                .map(world::getTileEntity)
                .filter(te -> te != null && !(te instanceof StorageScannerTileEntity) && !(te instanceof CraftingManagerTileEntity))
                .iterator();
        while (!stack.isEmpty() && iterator.hasNext()) {
            TileEntity te = iterator.next();
            stack = InventoryHelper.insertItem(world, te.getPos(), null, stack);
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
    public void giveToPlayerFromScreen(ItemStack stack, boolean single, PlayerEntity player, boolean oredict) {
        if (stack.isEmpty()) {
            return;
        }
        if (getStoredPower() < StorageScannerConfiguration.rfPerRequest.get()) {
            player.sendStatusMessage(new StringTextComponent(TextFormatting.RED + "Not enough power to request items!"), false);
            return;
        }

        Set<Integer> oredictMatches = getOredictMatchers(stack, oredict);
        final int[] cnt = {single ? 1 : stack.getMaxStackSize()};
        int orig = cnt[0];
        inventories.stream()
                .filter(this::isOutputFromScreen)
                .map(this::getItemHandlerAt)
                .forEachOrdered(handler -> {
                    handler.ifPresent(h -> {
                        for (int i = 0; i < h.getSlots(); i++) {
                            ItemStack itemStack = h.getStackInSlot(i);
                            if (isItemEqual(stack, itemStack, oredictMatches)) {
                                ItemStack received = h.extractItem(i, cnt[0], false);
                                giveItemToPlayer(player, cnt, received);
                            }
                        }
                    });
                });
        if (orig != cnt[0]) {
            consumeEnergy(StorageScannerConfiguration.rfPerRequest.get());
            SoundTools.playSound(world, SoundEvents.ENTITY_ITEM_PICKUP, getPos().getX(), getPos().getY(), getPos().getZ(), 1.0f, 1.0f);
        }
    }

    private boolean giveItemToPlayer(PlayerEntity player, int[] cnt, ItemStack received) {
        if (!received.isEmpty() && cnt[0] > 0) {
            cnt[0] -= received.getCount();
            giveToPlayer(received, player);
            return true;
        }
        return false;
    }

    private boolean giveToPlayer(ItemStack stack, PlayerEntity player) {
        if (stack.isEmpty()) {
            return false;
        }
        if (!player.inventory.addItemStackToInventory(stack)) {
            player.entityDropItem(stack, 1.05f);
        }
        return true;
    }

    @Override
    public int countItems(Predicate<ItemStack> matcher, boolean starred, @Nullable Integer maxneeded) {
        final int[] cc = {0};
        inventories.stream()
                .filter(p -> isValid(p) && ((!starred) || isRoutable(p)) && WorldTools.chunkLoaded(world, p))
                .map(world::getTileEntity)
                .filter(te -> te != null && !(te instanceof StorageScannerTileEntity) && !(te instanceof CraftingManagerTileEntity))
                .allMatch(te -> {
                    InventoryHelper.getItems(te, matcher)
                            .forEach(s -> cc[0] += s.getCount());
                    if (maxneeded != null && cc[0] >= maxneeded) {
                        return false;
                    }
                    return true;
                });
        return cc[0];
    }


    @Override
    public int countItems(ItemStack match, boolean routable, boolean oredict) {
        return countItems(match, routable, oredict, null);
    }

    @Override
    public int countItems(ItemStack stack, boolean starred, boolean oredict, @Nullable Integer maxneeded) {
        if (stack.isEmpty()) {
            return 0;
        }
        Set<Integer> oredictMatches = getOredictMatchers(stack, oredict);
        Iterator<TileEntity> iterator = inventories.stream()
                .filter(p -> isValid(p) && ((!starred) || isRoutable(p)) && WorldTools.chunkLoaded(world, p))
                .map(world::getTileEntity)
                .filter(te -> te != null && !(te instanceof StorageScannerTileEntity) && !(te instanceof CraftingManagerTileEntity))
                .iterator();

        int cnt = 0;
        while (iterator.hasNext()) {
            TileEntity te = iterator.next();
            Integer cachedCount = null;
            if (te instanceof IInventoryTracker) {
                IInventoryTracker tracker = (IInventoryTracker) te;
                CachedItemCount itemCount = cachedCounts.get(new CachedItemKey(te.getPos(), stack.getItem(), 0 /* @todo 1.14 stack.getMetadata()*/));
                if (itemCount != null) {
                    Integer oldVersion = itemCount.getVersion();
                    if (oldVersion == tracker.getVersion()) {
                        cachedCount = itemCount.getCount();
                    }
                }
            }
            if (cachedCount != null) {
                cnt += cachedCount;
            } else {
                final int[] cc = {0};
                InventoryHelper.getItems(te, s -> isItemEqual(stack, s, oredictMatches))
                        .forEach(s -> cc[0] += s.getCount());
                if (te instanceof IInventoryTracker) {
                    IInventoryTracker tracker = (IInventoryTracker) te;
                    cachedCounts.put(new CachedItemKey(te.getPos(), stack.getItem(), 0 /* @todc 1.14 meta */), new CachedItemCount(tracker.getVersion(), cc[0]));
                }
                cnt += cc[0];
            }
            if (maxneeded != null && cnt >= maxneeded) {
                break;
            }
        }

        return cnt;
    }

    private static Set<Integer> getOredictMatchers(ItemStack stack, boolean oredict) {
        Set<Integer> oredictMatches = new HashSet<>();
        // @todo 1.14 tags
//        if (oredict) {
//            for (int id : OreDictionary.getOreIDs(stack)) {
//                oredictMatches.add(id);
//            }
//        }
        return oredictMatches;
    }

    public static boolean isItemEqual(ItemStack thisItem, ItemStack other, boolean oredict) {
        return isItemEqual(thisItem, other, getOredictMatchers(thisItem, oredict));
    }

    public static boolean isItemEqual(ItemStack thisItem, ItemStack other, Set<Integer> oreDictMatchers) {
        if (other.isEmpty()) {
            return false;
        }
        if (oreDictMatchers.isEmpty()) {
            return thisItem.isItemEqual(other);
        } else {
            // @todo 1.14
//            int[] oreIDs = OreDictionary.getOreIDs(other);
//            for (int id : oreIDs) {
//                if (oreDictMatchers.contains(id)) {
//                    return true;
//                }
//            }
        }
        return false;
    }


    public Set<BlockPos> performSearch(String search) {

        Predicate<ItemStack> matcher = getMatcher(search);

        Set<BlockPos> output = new HashSet<>();
        Predicate<ItemStack> finalMatcher = matcher;
        inventories.stream()
                .filter(this::isValid)
                .map(world::getTileEntity)
                .filter(te -> te != null && !(te instanceof StorageScannerTileEntity))
                .forEach(te -> InventoryHelper.getItems(te, finalMatcher).forEach(s -> output.add(te.getPos())));
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
            return s -> BlockTools.getModid(s).toLowerCase().startsWith(split.substring(1));
        } else {
            return s -> s.getDisplayName().getFormattedText().toLowerCase().contains(split);
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
        markDirtyClient();
    }

    public boolean isOpenWideView() {
        return openWideView;
    }

    public void setOpenWideView(boolean openWideView) {
        this.openWideView = openWideView;
        markDirtyClient();
    }

    public boolean isExportToCurrent() {
        return exportToCurrent;
    }

    public void setExportToCurrent(boolean exportToCurrent) {
        this.exportToCurrent = exportToCurrent;
        markDirtyClient();
    }

    private void toggleExportRoutable() {
        exportToCurrent = !exportToCurrent;
        markDirtyClient();
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
        markDirtyClient();
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
        markDirty();
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
        markDirty();
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
        markDirty();
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
        markDirty();
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
        markDirty();
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

        for (BlockPos p : old) {
            if (xnetAccess.containsKey(p) || inRange(p)) {
                TileEntity te = world.getTileEntity(p);
                if (te != null && !(te instanceof StorageScannerTileEntity)) {
                    if (te instanceof CraftingManagerTileEntity) {
                        if (seenPositions.add(p)) {
                            inventories.add(p);
                            craftingInventories.add(p);
                            oldAdded.add(p);
                        }
                    } else {
                        te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
                            if (seenPositions.add(p)) {
                                inventories.add(p);
                                oldAdded.add(p);
                            }
                        });
                    }
                }
            }
        }

        // Now append all inventories that are new.
        for (int x = getPos().getX() - radius; x <= getPos().getX() + radius; x++) {
            for (int z = getPos().getZ() - radius; z <= getPos().getZ() + radius; z++) {
                for (int y = getPos().getY() - radius; y <= getPos().getY() + radius; y++) {
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
                if (world.getTileEntity(pos) instanceof CraftingManagerTileEntity) {
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
            TileEntity te = world.getTileEntity(p);
            if (te != null && !(te instanceof StorageScannerTileEntity)) {
                if (te instanceof CraftingManagerTileEntity) {
                    if (seenPositions.add(p)) {
                        inventories.add(p);
                        craftingInventories.add(p);
                    }
                } else if (!inventories.contains(p)) {
                    te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
                        if (seenPositions.add(p)) {
                            inventories.add(p);
                        }
                    });
                }
            }
        }
    }

    private boolean inRange(BlockPos p) {
        return p.getX() >= getPos().getX() - radius && p.getX() <= getPos().getX() + radius && p.getY() >= getPos().getY() - radius && p.getY() <= getPos().getY() + radius && p.getZ() >= getPos().getZ() - radius && p.getZ() <= getPos().getZ() + radius;
    }

    private static final ItemStack DUMMY = new ItemStack(Items.BEDROCK, 666);

    @Override
    public ItemStack requestItem(Predicate<ItemStack> matcher, boolean simulate, int amount, boolean doRoutable) {
        if (getStoredPower() < StorageScannerConfiguration.rfPerRequest.get()) {
            return ItemStack.EMPTY;
        }
        return inventories.stream()
                .filter(p -> isOutputFromAuto(p) && ((!doRoutable) || isRoutable(p)))
                .filter(p -> !(world.getTileEntity(p) instanceof CraftingManagerTileEntity))
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
    public ItemStack requestItem(ItemStack match, int amount, boolean doRoutable, boolean oredict) {
        if (match.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (getStoredPower() < StorageScannerConfiguration.rfPerRequest.get()) {
            return ItemStack.EMPTY;
        }

        Set<Integer> oredictMatches = getOredictMatchers(match, oredict);
        final ItemStack[] result = {ItemStack.EMPTY};
        final int[] cnt = {match.getMaxStackSize() < amount ? match.getMaxStackSize() : amount};
        inventories.stream()
                .filter(p -> isOutputFromAuto(p) && (!doRoutable) || isRoutable(p))
                .filter(p -> !(world.getTileEntity(p) instanceof CraftingManagerTileEntity))
                .map(this::getItemHandlerAt)
                .allMatch(handler -> {
                    handler.ifPresent(h -> {
                        for (int i = 0; i < h.getSlots(); i++) {
                            ItemStack itemStack = h.getStackInSlot(i);
                            if (isItemEqual(match, itemStack, oredictMatches)) {
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
        if (!WorldTools.chunkLoaded(world, p)) {
            return LazyOptional.empty();
        }
        TileEntity te = world.getTileEntity(p);
        if (te == null || te instanceof StorageScannerTileEntity) {
            return LazyOptional.empty();
        }
        return getItemHandlerAt(te, null);
    }

    // @todo move to McJtyLib
    @Nonnull
    private static LazyOptional<IItemHandler> getItemHandlerAt(@Nullable TileEntity te, Direction intSide) {
        if (te != null) {
            return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, intSide);
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

        Iterator<LazyOptional<IItemHandler>> iterator = inventories.stream()
                .filter(p -> isInputFromAuto(p) && (!p.equals(getPos()) && isRoutable(p) && getInputMatcher(p).test(stack)))
                .filter(p -> !(world.getTileEntity(p) instanceof CraftingManagerTileEntity))
                .map(this::getItemHandlerAt)
                .filter(i -> i.isPresent())
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
        TileEntity tileEntity = world.getTileEntity(invPos);
        if (tileEntity instanceof StorageScannerTileEntity) {
            return outSlot;
        }

        int size = InventoryHelper.getInventorySize(tileEntity);

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
     *   them and requestCraft consumers will be fired for all missing items
     * - Returns a list of itemstacks as extracted from the storage scanner. The craft can go on
     *
     * If the extract is true the requested items are actually extracted in case they are available. Otherwise
     * nothing happens (and an empty list will be returned or null)
     */
    @Nullable
    public List<ItemStack> requestIngredients(List<Ingredient> ingredients, Consumer<Ingredient> missingIngredientConsumer, boolean extract) {
        List<Ingredient> missing = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            ItemStack stack = requestItem(ingredient, true, 1, true);
            if (stack.isEmpty()) {
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
                if (!getAllInventories().anyMatch(p -> {
                    TileEntity te = world.getTileEntity(p);
                    if (te instanceof CraftingManagerTileEntity) {
                        CraftingManagerTileEntity craftingManager = (CraftingManagerTileEntity) te;
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
    public void requestCraft(BlockPos invPos, ItemStack requested, int amount, PlayerEntity player) {
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
    public void requestStack(BlockPos invPos, ItemStack requested, int amount, PlayerEntity player) {
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

        itemHandler.ifPresent(h -> {
            ItemStack outSlot = h.getStackInSlot(StorageScannerContainer.SLOT_OUT);
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
                Iterator<BlockPos> iterator = inventories.stream()
                        .filter(p -> !(world.getTileEntity(p) instanceof CraftingManagerTileEntity))
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
            h.setStackInSlot(StorageScannerContainer.SLOT_OUT, outSlot);

            if (StorageScannerConfiguration.requestStraightToInventory.get()) {
                if (player.inventory.addItemStackToInventory(outSlot)) {
                    h.setStackInSlot(StorageScannerContainer.SLOT_OUT, ItemStack.EMPTY);
                }
            }
        });
    }

    private void addItemStack(List<ItemStack> stacks, Set<Item> foundItems, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (foundItems.contains(stack.getItem())) {
            for (ItemStack s : stacks) {
                if (ItemHandlerHelper.canItemStacksStack(s.getStack(), stack)) {
                    s.getStack().grow(stack.getCount());
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
        TileEntity tileEntity = world.getTileEntity(cpos);
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

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        craftingSystem.read(tagCompound.getCompound("CS"));
        ListNBT list = tagCompound.getList("inventories", Constants.NBT.TAG_COMPOUND);
        inventories.clear();
        craftingInventories = null;
        for (INBT inbt : list) {
            CompoundNBT tag = (CompoundNBT) inbt;
            BlockPos c = BlockPosTools.read(tag, "c");
            inventories.add(c);
        }
        list = tagCompound.getList("routable", Constants.NBT.TAG_COMPOUND);
        routable.clear();
        for (INBT inbt : list) {
            CompoundNBT tag = (CompoundNBT) inbt;
            BlockPos c = BlockPosTools.read(tag, "c");
            routable.add(c);
        }
        list = tagCompound.getList("fromxnet", Constants.NBT.TAG_COMPOUND);
        inventoriesFromXNet.clear();
        for (INBT inbt : list) {
            CompoundNBT tag = (CompoundNBT) inbt;
            BlockPos c = BlockPosTools.read(tag, "c");
            inventoriesFromXNet.add(c);
        }
    }

    @Override
    protected void readInfo(CompoundNBT tagCompound) {
        super.readInfo(tagCompound);
        if (tagCompound.contains("Info")) {
            CompoundNBT infoTag = tagCompound.getCompound("Info");
            radius = infoTag.getInt("radius");
            exportToCurrent = infoTag.getBoolean("exportC");
            if (infoTag.contains("wideview")) {
                openWideView = infoTag.getBoolean("wideview");
            } else {
                openWideView = true;
            }
            craftingGrid.readFromNBT(infoTag.getCompound("grid"));
        } else {
            openWideView = true;
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        tagCompound.put("CS", craftingSystem.write());
        ListNBT list = new ListNBT();
        for (BlockPos c : inventories) {
            CompoundNBT tag = BlockPosTools.write(c);
            list.add(tag);
        }
        tagCompound.put("inventories", list);
        list = new ListNBT();
        for (BlockPos c : routable) {
            CompoundNBT tag = BlockPosTools.write(c);
            list.add(tag);
        }
        tagCompound.put("routable", list);
        list = new ListNBT();
        for (BlockPos c : inventoriesFromXNet) {
            CompoundNBT tag = BlockPosTools.write(c);
            list.add(tag);
        }
        tagCompound.put("fromxnet", list);
        return tagCompound;
    }

    @Override
    protected void writeInfo(CompoundNBT tagCompound) {
        super.writeInfo(tagCompound);
        CompoundNBT infoTag = getOrCreateInfo(tagCompound);
        infoTag.putInt("radius", radius);
        infoTag.putBoolean("exportC", exportToCurrent);
        infoTag.putBoolean("wideview", openWideView);
        infoTag.put("grid", craftingGrid.writeToNBT());
    }

    private void clearGrid() {
        CraftingGridInventory inventory = craftingGrid.getCraftingGridInventory();
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
        markDirty();
    }

    @Override
    public boolean execute(PlayerEntity playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_UP.equals(command)) {
            moveUp(params.get(PARAM_INDEX));
            return true;
        } else if (CMD_TOP.equals(command)) {
            moveTop(params.get(PARAM_INDEX));
            return true;
        } else if (CMD_DOWN.equals(command)) {
            moveDown(params.get(PARAM_INDEX));
            return true;
        } else if (CMD_BOTTOM.equals(command)) {
            moveBottom(params.get(PARAM_INDEX));
            return true;
        } else if (CMD_REMOVE.equals(command)) {
            removeInventory(params.get(PARAM_INDEX));
            return true;
        } else if (CMD_TOGGLEROUTABLE.equals(command)) {
            toggleRoutable(params.get(PARAM_POS));
            return true;
        } else if (CMD_SETVIEW.equals(command)) {
            setOpenWideView(params.get(PARAM_VIEW));
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public TypedMap executeWithResult(String command, TypedMap args) {
        TypedMap result = super.executeWithResult(command, args);
        if (result != null) {
            return result;
        }
        if (CMD_SCANNER_INFO.equals(command)) {
            return TypedMap.builder()
                    .put(PARAM_ENERGY, getStoredPower())
                    .put(PARAM_EXPORT, isExportToCurrent())
                    .build();
        }
        return null;
    }

    @Override
    public boolean receiveDataFromServer(String command, @Nonnull TypedMap result) {
        boolean rc = super.receiveDataFromServer(command, result);
        if (rc) {
            return rc;
        }
        if (CMD_SCANNER_INFO.equals(command)) {
            rfReceived = result.get(PARAM_ENERGY);
            exportToCurrentReceived = result.get(PARAM_EXPORT);
            return true;
        }
        return false;
    }

    /**
     * Return true if this is a dummy TE. i.e. this happens only when accessing a
     * storage scanner in a tablet on the client side.
     */
    public boolean isDummy() {
        return monitorDim != null;
    }

    /**
     * This is used client side only for the GUI.
     * Return the position of the crafting grid container. This
     * is either the position of this tile entity (in case we are just looking
     * directly at the storage scanner), the position of a 'watching' tile
     * entity (in case we are a dummy for the storage terminal) or else null
     * in case we're using a handheld item.
     *
     * @return
     */
    public BlockPos getCraftingGridContainerPos() {
        return getPos();
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
     *
     * @return
     */
    public BlockPos getStorageScannerPos() {
        return getPos();
    }

    public CraftingSystem getCraftingSystem() {
        return craftingSystem;
    }

    public DimensionType getDimension() {
        if (isDummy()) {
            return monitorDim;
        } else {
            return world.getDimension().getType();
        }
    }

    @Nonnull
    private IInformationScreenInfo createScreenInfo() {
        return new StorageScannerInformationScreenInfo(this);
    }

    @Nonnull
    private NoDirectionItemHander createItemHandler() {
        return new NoDirectionItemHander(StorageScannerTileEntity.this, CONTAINER_FACTORY) {

            @Override
            public boolean isItemInsertable(int slot, @Nonnull ItemStack stack) {
                return slot == StorageScannerContainer.SLOT_IN_AUTO;
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandler.cast();
        }
        if (cap == CapabilityEnergy.ENERGY) {
            return energyHandler.cast();
        }
        if (cap == CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY) {
            return screenHandler.cast();
        }
        if (cap == CapabilityInfusable.INFUSABLE_CAPABILITY) {
            return infusableHandler.cast();
        }
        if (cap == CapabilityInformationScreenInfo.INFORMATION_SCREEN_INFO_CAPABILITY) {
            return infoScreenInfo.cast();
        }
        return super.getCapability(cap, facing);
    }

}
