package mcjty.rftoolsstorage.modules.modularstorage.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcjty.lib.base.StyleConfig;
import mcjty.lib.client.GuiTools;
import mcjty.lib.container.BaseSlot;
import mcjty.lib.container.GhostOutputSlot;
import mcjty.lib.container.GhostSlot;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.WindowManager;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbase.RFToolsBase;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.craftinggrid.CraftingGridProvider;
import mcjty.rftoolsstorage.craftinggrid.GuiCraftingGrid;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageConfiguration;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageModule;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageTileEntity;
import mcjty.rftoolsstorage.modules.modularstorage.items.StorageModuleItem;
import mcjty.rftoolsstorage.setup.CommandHandler;
import mcjty.rftoolsstorage.setup.RFToolsStorageMessages;
import mcjty.rftoolsstorage.storage.modules.DefaultTypeModule;
import mcjty.rftoolsstorage.storage.modules.TypeModule;
import mcjty.rftoolsstorage.storage.sorters.ItemSorter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static mcjty.lib.gui.layout.AbstractLayout.DEFAULT_HORIZONTAL_MARGIN;
import static mcjty.lib.gui.widgets.Widgets.*;
import static mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer.SLOT_STORAGE;
import static mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageTileEntity.*;


public class GuiModularStorage extends GenericGuiContainer<ModularStorageTileEntity, ModularStorageContainer> {
    public static final int STORAGE_WIDTH = 256;

    public static final String VIEW_LIST = "list";
    public static final String VIEW_COLUMNS = "columns";
    public static final String VIEW_ICONS = "icons";

    private TypeModule typeModule;

    private static final ResourceLocation iconLocationTop = new ResourceLocation(RFToolsStorage.MODID, "textures/gui/modularstoragetop.png");
    private static final ResourceLocation iconLocation = new ResourceLocation(RFToolsStorage.MODID, "textures/gui/modularstorage.png");
    private static final ResourceLocation guiElements = new ResourceLocation(RFToolsBase.MODID, "textures/gui/guielements.png");

    private WidgetList itemList;
    private TextField filter;
    private ImageChoiceLabel viewMode;
    private ImageChoiceLabel sortMode;
    private ImageChoiceLabel groupMode;
    private Label amountLabel;
    private ToggleButton lockButton;
    private Button cycleButton;
    private Button compactButton;
    private Label warningLabel;

    private GuiCraftingGrid craftingGrid;

    public GuiModularStorage(ModularStorageTileEntity tileEntity, ModularStorageContainer container, PlayerInventory inventory) {
        super(tileEntity, container, inventory, ModularStorageModule.MODULAR_STORAGE.get().getManualEntry());

        craftingGrid = new GuiCraftingGrid();

        xSize = STORAGE_WIDTH;

        double height = Minecraft.getInstance().getMainWindow().getScaledHeight();

        if (height > 510) {
            ySize = ModularStorageConfiguration.height3.get();
        } else if (height > 340) {
            ySize = ModularStorageConfiguration.height2.get();
        } else {
            ySize = ModularStorageConfiguration.height1.get();
        }
    }

    public static void register() {
        register(ModularStorageModule.CONTAINER_MODULAR_STORAGE.get(), GuiModularStorage::new);
    }

    @Override
    public void init() {
        super.init();

        itemList = list(5, 3, 235, ySize - 89).name("items").noSelectionMode(true).userObject(new Integer(-1)).
                leftMargin(0).rowheight(-1);
        Slider slider = slider(241, 3, 11, ySize - 89).desiredWidth(11).vertical()
                .scrollableName("items");

        warningLabel = label(20, 20, 200, 20, "Lock the storage to access the items!");
        lockButton = new ToggleButton().hint(5, ySize - 23-18, 16, 16)
                .text("L")
                .name("lock")
                .event(this::updateSettings)
                .tooltips("Lock/unlock the module slots");
        cycleButton = button(5, ySize - 23, 16, 16, "C")
                .name("cycle")
                .channel("cycle")
                .tooltips("Cycle to the next storage module");

        Panel modePanel = setupModePanel();

        Panel toplevel = Widgets.positional().children(itemList, slider, modePanel, lockButton, cycleButton, warningLabel);

        toplevel.setBackgrounds(iconLocationTop, iconLocation);
        toplevel.setBackgroundLayout(false, ySize - ModularStorageConfiguration.height1.get() + 2);

        if (tileEntity == null) {
            // We must hide three slots.
            ImageLabel hideLabel = new ImageLabel();
            hideLabel.hint(4, ySize - 26 - 3 * 18, 20, 55);
            hideLabel.image(guiElements, 32, 32);
            toplevel.children(hideLabel);
        }

        toplevel.bounds(guiLeft, guiTop, xSize, ySize);

        window = new Window(this, toplevel);

        window.event("cycle", (source, params) -> cycleStorage());
        window.event("compact", (source, params) -> compact());

        if (ModularStorageConfiguration.autofocusSearch.get()) {
            window.setTextFocus(filter);
        }

        CraftingGridProvider provider = null;
        BlockPos pos = null;
        if (tileEntity != null) {
            provider = tileEntity;
            pos = tileEntity.getPos();
// @todo 1.14
//        } else if (inventorySlots instanceof ModularStorageItemContainer) {
//            ModularStorageItemContainer storageItemContainer = (ModularStorageItemContainer) inventorySlots;
//            provider = storageItemContainer.getCraftingGridProvider();
//        } else if (inventorySlots instanceof RemoteStorageItemContainer) {
//            RemoteStorageItemContainer storageItemContainer = (RemoteStorageItemContainer) inventorySlots;
//            provider = storageItemContainer.getCraftingGridProvider();
        } else {
            throw new RuntimeException("Should not happen!");
        }

        craftingGrid.initGui(RFToolsStorageMessages.INSTANCE, minecraft, this, pos, tileEntity.getDimension(), provider, guiLeft, guiTop, xSize, ySize);
        sendServerCommand(RFToolsStorageMessages.INSTANCE, RFToolsStorage.MODID, CommandHandler.CMD_REQUEST_GRID_SYNC, TypedMap.builder()
                .put(CommandHandler.PARAM_POS, pos)
                .put(CommandHandler.PARAM_DIMENSION, tileEntity.getDimension())
                .build());
    }

    @Override
    protected void registerWindows(WindowManager mgr) {
        super.registerWindows(mgr);
        mgr.addWindow(craftingGrid.getWindow());
    }

    private Panel setupModePanel() {
        filter = textfield(3, 3, 57, 13).tooltips("Name based filter for items")
                .event((newText) -> updateSettings());

        viewMode = new ImageChoiceLabel().hint(4, 19, 16, 16).tooltips("Control how items are shown", "in the view")
                .event((newChoice) -> updateSettings());
        viewMode.choice(VIEW_LIST, "Items are shown in a list view", guiElements, 9 * 16, 16);
        viewMode.choice(VIEW_COLUMNS, "Items are shown in columns", guiElements, 10 * 16, 16);
        viewMode.choice(VIEW_ICONS, "Items are shown with icons", guiElements, 11 * 16, 16);

        updateTypeModule();

        sortMode = new ImageChoiceLabel().hint(23, 19, 16, 16).tooltips("Control how items are sorted", "in the view")
                .event((newChoice) -> updateSettings());
        for (ItemSorter sorter : typeModule.getSorters()) {
            sortMode.choice(sorter.getName(), sorter.getTooltip(), guiElements, sorter.getU(), sorter.getV());
        }

        groupMode = new ImageChoiceLabel().hint(42, 19, 16, 16).tooltips("If enabled it will show groups", "based on sorting criterium")
                .event((newChoice) -> updateSettings());
        groupMode.choice("Off", "Don't show groups", guiElements, 13 * 16, 0);
        groupMode.choice("On", "Show groups", guiElements, 14 * 16, 0);

        amountLabel = label(16, 40, 66, 12, "?/?")
                .horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                .tooltips("Amount of stacks / maximum amount");

        compactButton = button(4, 39, 12, 12, "z")
                .name("compact")
                .channel("compact")
                .tooltips("Compact equal stacks");

        if (tileEntity != null) {
            filter.text(ModularStorageConfiguration.clearSearchOnOpen.get() ? "" : tileEntity.getFilter());
            setViewMode(tileEntity.getViewMode());
            setSortMode(tileEntity.getSortMode());
            groupMode.setCurrentChoice(tileEntity.isGroupMode() ? 1 : 0);
            lockButton.pressed(tileEntity.isLocked());
            warningLabel.visible(!tileEntity.isLocked());
            itemList.visible(tileEntity.isLocked());
        } else {
            ItemStack heldItem = minecraft.player.getHeldItem(Hand.MAIN_HAND);
            if (!heldItem.isEmpty() && heldItem.hasTag()) {
                CompoundNBT tagCompound = heldItem.getTag();
                filter.text(ModularStorageConfiguration.clearSearchOnOpen.get() ? "" : tagCompound.getString("filter"));
                setViewMode(tagCompound.getString("viewMode"));
                setSortMode(tagCompound.getString("sortMode"));
                groupMode.setCurrentChoice(tagCompound.getBoolean("groupMode") ? 1 : 0);
            }
        }

        return Widgets.positional().hint(24, ySize - 80, 64, 77)
                .filledRectThickness(-2)
                .filledBackground(StyleConfig.colorListBackground)
                .children(filter, viewMode, sortMode, groupMode, amountLabel, compactButton);
    }

    private void setSortMode(String sortMode) {
        int idx;
        idx = this.sortMode.findChoice(sortMode);
        if (idx == -1) {
            this.sortMode.setCurrentChoice(0);
        } else {
            this.sortMode.setCurrentChoice(idx);
        }
    }

    private void setViewMode(String viewMode) {
        int idx = this.viewMode.findChoice(viewMode);
        if (idx == -1) {
            this.viewMode.setCurrentChoice(VIEW_LIST);
        } else {
            this.viewMode.setCurrentChoice(idx);
        }
    }

    private void cycleStorage() {
        if (tileEntity != null) {
            window.sendAction(RFToolsStorageMessages.INSTANCE, tileEntity, ACTION_CYCLE);
        } else {
            sendServerCommand(RFToolsStorageMessages.INSTANCE, RFToolsStorage.MODID, CommandHandler.CMD_CYCLE_STORAGE);
        }
    }

    private void compact() {
        if (tileEntity != null) {
            window.sendAction(RFToolsStorageMessages.INSTANCE, tileEntity, ACTION_COMPACT);
        } else {
            sendServerCommand(RFToolsStorageMessages.INSTANCE, RFToolsStorage.MODID, CommandHandler.CMD_COMPACT);
        }
    }

    private void updateSettings() {
        if (tileEntity != null) {
            tileEntity.setSortMode(sortMode.getCurrentChoice());
            tileEntity.setViewMode(viewMode.getCurrentChoice());
            tileEntity.setFilter(filter.getText());
            tileEntity.setGroupMode(groupMode.getCurrentChoiceIndex() == 1);
            tileEntity.setLocked(lockButton.isPressed());
            warningLabel.visible(!tileEntity.isLocked());
            itemList.visible(tileEntity.isLocked());
            sendServerCommandTyped(RFToolsStorageMessages.INSTANCE, CMD_SETTINGS,
                    TypedMap.builder()
                            .put(PARAM_SORTMODE, sortMode.getCurrentChoice())
                            .put(PARAM_VIEWMODE, viewMode.getCurrentChoice())
                            .put(PARAM_FILTER, filter.getText())
                            .put(PARAM_GROUPMODE, groupMode.getCurrentChoiceIndex() == 1)
                            .put(PARAM_LOCKED, lockButton.isPressed())
                            .build());
        } else {
            // @todo 1.14
//            RFToolsStorageMessages.INSTANCE.sendToServer(new PacketUpdateNBTItemStorage(
//                    TypedMap.builder()
//                            .put(new Key<>("sortMode", Type.STRING), sortMode.getCurrentChoice())
//                            .put(new Key<>("viewMode", Type.STRING), viewMode.getCurrentChoice())
//                            .put(new Key<>("filter", Type.STRING), filter.getText())
//                            .put(new Key<>("groupMode", Type.BOOLEAN), groupMode.getCurrentChoiceIndex() == 1)
//                            .build()));
        }
    }

    private Slot findEmptySlot() {
        for (Object slotObject : container.inventorySlots) {
            Slot slot = (Slot) slotObject;
            // Skip the first two slots if we are on a modular storage block.
//            if (tileEntity != null && slot.getSlotIndex() < SLOT_STORAGE) {
            if (tileEntity != null && !(slot instanceof BaseSlot)) {
                continue;
            }
            if ((!slot.getHasStack()) || slot.getStack().getCount() == 0) {
                return slot;
            }
        }
        return null;
    }


    @Override
    public boolean isSlotSelected(Slot slotIn, double x, double y) {
        if (slotIn instanceof BaseSlot && ((BaseSlot) slotIn).getTe() instanceof ModularStorageTileEntity){ // @todo 1.14 || slotIn.inventory instanceof ModularStorageItemInventory || slotIn.inventory instanceof RemoteStorageItemInventory) {
            Widget<?> widget = window.getToplevel().getWidgetAtPosition(x, y);
            if (widget instanceof BlockRender) {
                Object userObject = widget.getUserObject();
                if (userObject instanceof Integer) {
                    Integer slotIndex = (Integer) userObject;
                    return slotIndex == slotIn.slotNumber;
                }
            } else {
                return super.isSlotSelected(slotIn, x, y);
            }
            return false;
        } else {
            return super.isSlotSelected(slotIn, x, y);
        }
    }

    @Override
    public Slot getSelectedSlot(double x, double y) {
        Widget<?> widget = window.getToplevel().getWidgetAtPosition(x, y);
        if (widget != null) {
            Object userObject = widget.getUserObject();
            if (userObject instanceof Integer) {
                Integer slotIndex = (Integer) userObject;
                if (slotIndex != -1) {
                    return container.getSlot(slotIndex);
                } else {
                    return findEmptySlot();
                }
            }
        }

        return super.getSelectedSlot(x, y);
    }

    private void dumpClasses(String name, Object o) {
        Logging.log(name + ":" + o.getClass().getCanonicalName());
        Class<?>[] classes = o.getClass().getClasses();
        for (Class<?> a : classes) {
            Logging.log("        " + a.getCanonicalName());
        }
        Logging.log("        Super:" + o.getClass().getGenericSuperclass());
        for (java.lang.reflect.Type type : o.getClass().getGenericInterfaces()) {
            Logging.log("        type:" + type.getClass().getCanonicalName());
        }

    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        // @todo 1.14
//        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
//            Slot slot = getSlotAtPosition(x, y);
//            if (slot != null && slot.getHasStack()) {
//                ItemStack stack = slot.getStack();
//                Item item = stack.getItem();
//                if (item instanceof ItemBlock) {
//                    Block block = ((ItemBlock) item).getBlock();
//                    dumpClasses("Block", block);
//                } else {
//                    dumpClasses("Item", item);
//                }
//            }
//        }
        boolean r = super.mouseClicked(x, y, button);
//        craftingGrid.getWindow().mouseClicked(x, y, button);
        if (button == 1) {
            Slot slot = getSelectedSlot(x, y);
            if (slot instanceof GhostOutputSlot) {
                if (tileEntity != null) {
                    window.sendAction(RFToolsStorageMessages.INSTANCE, tileEntity, ModularStorageTileEntity.ACTION_CLEARGRID);
                } else {
                    sendServerCommand(RFToolsStorageMessages.INSTANCE, RFToolsStorage.MODID, CommandHandler.CMD_CLEAR_GRID);
                }
            }
        }
        return r;
    }

    private void updateList() {
        itemList.removeChildren();

        if (tileEntity != null && !container.getSlot(ModularStorageContainer.SLOT_STORAGE_MODULE).getHasStack()) {
            amountLabel.text("(empty)");
            compactButton.enabled(false);
            cycleButton.enabled(false);
            return;
        }

        cycleButton.enabled(isTabletWithRemote() || isRemote());

        String filterText = filter.getText().toLowerCase().trim();

        String view = viewMode.getCurrentChoice();
        int numcolumns;
        int labelWidth;
        int spacing;
        if (VIEW_LIST.equals(view)) {
            numcolumns = 1;
            labelWidth = 210;
            spacing = 5;
        } else if (VIEW_COLUMNS.equals(view)) {
            numcolumns = 2;
            labelWidth = 86;
            spacing = 5;
        } else {
            numcolumns = 12;
            labelWidth = 0;
            spacing = 3;
        }

        AtomicInteger max = new AtomicInteger();
        List<Pair<ItemStack, Integer>> items = new ArrayList<>();
        if (tileEntity != null) {
            tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
                for (int i = 0 ; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        String displayName = stack.getDisplayName().getString() /* was getFormattedText() */;
                        if (filterText.isEmpty() || displayName.toLowerCase().contains(filterText)) {
                            items.add(Pair.of(stack, i + SLOT_STORAGE));
                        }
                    }
                }
                max.set(handler.getSlots());

            });
//        } else {
//            // Also works for ModularStorageItemContainer
//            for (int i = 0; i < RemoteStorageItemContainer.MAXSIZE_STORAGE; i++) {
//                Slot slot = inventorySlots.getSlot(i);
//                ItemStack stack = slot.getStack();
//                if (!stack.isEmpty()) {
//                    String displayName = stack.getDisplayName();
//                    if (filterText.isEmpty() || displayName.toLowerCase().contains(filterText)) {
//                        items.add(Pair.of(stack, i));
//                    }
//                }
//            }
//            ItemStack heldItem = mc.player.getHeldItem(EnumHand.MAIN_HAND);
//            if (!heldItem.isEmpty() && heldItem.hasTagCompound()) {
//                max = heldItem.getTagCompound().getInteger("maxSize");
//            } else {
//                max = 0;
//            }
        }
        amountLabel.text(items.size() + "/" + max);
        compactButton.enabled(max.get() > 0);

        int sort = getCurrentSortMode();

        boolean dogroups = groupMode.getCurrentChoiceIndex() == 1;

        ItemSorter itemSorter = typeModule.getSorters().get(sort);
        Collections.sort(items, itemSorter.getComparator());

        Pair<Panel, Integer> currentPos = MutablePair.of(null, 0);
        Pair<ItemStack, Integer> prevItem = null;
        for (Pair<ItemStack, Integer> item : items) {
            currentPos = addItemToList(item.getKey(), itemList, currentPos, numcolumns, labelWidth, spacing, item.getValue(),
                    dogroups && (prevItem == null || !itemSorter.isSameGroup(prevItem, item)), itemSorter.getGroupName(item));
            prevItem = item;
        }

        int newfirst = -1;
        if (itemList.getCountSelected() == 0) {
            if (itemList.getBounds() != null) {
                itemList.setFirstSelected(0);
                newfirst = itemList.getChildCount() - itemList.getCountSelected();
                if (newfirst < 0) {
                    newfirst = 0;
                }
            }
        } else if (itemList.getFirstSelected() > (itemList.getChildCount() - itemList.getCountSelected())) {
            newfirst = itemList.getChildCount() - itemList.getCountSelected();
        }
        if (newfirst >= 0) {
            itemList.setFirstSelected(newfirst);
        }
    }

    private boolean isRemote() {
        // @todo 1.14
//        ItemStack stack = inventorySlots.getSlot(ModularStorageContainer.SLOT_STORAGE_MODULE).getStack();
//        if (stack.isEmpty()) {
//            return false;
//        }
//        return stack.getItemDamage() == StorageModuleItem.STORAGE_REMOTE;
        return false;
    }

    private boolean isTabletWithRemote() {
        if (tileEntity != null) {
            return false;
        }
        ItemStack heldItem = minecraft.player.getHeldItem(Hand.MAIN_HAND);
        if (!heldItem.isEmpty() && heldItem.hasTag()) {
            int storageType = heldItem.getTag().getInt("childDamage");
            return storageType == StorageModuleItem.STORAGE_REMOTE;
        } else {
            return false;
        }
    }

    private int getCurrentSortMode() {
        updateTypeModule();
        String sortName = sortMode.getCurrentChoice();
        sortMode.clear();
        for (ItemSorter sorter : typeModule.getSorters()) {
            sortMode.choice(sorter.getName(), sorter.getTooltip(), guiElements, sorter.getU(), sorter.getV());
        }
        int sort = sortMode.findChoice(sortName);
        if (sort == -1) {
            sort = 0;
        }
        sortMode.setCurrentChoice(sort);
        return sort;
    }

    private void updateTypeModule() {
        if (tileEntity != null) {
            ItemStack typeStack = ItemStack.EMPTY; // @todo 1.14 tileEntity.getStackInSlot(ModularStorageContainer.SLOT_TYPE_MODULE);
            if (typeStack.isEmpty() || !(typeStack.getItem() instanceof TypeModule)) {
                typeModule = new DefaultTypeModule();
            } else {
                typeModule = (TypeModule) typeStack.getItem();
            }
        } else {
            typeModule = new DefaultTypeModule();
        }
    }

    private Pair<Panel, Integer> addItemToList(ItemStack stack, WidgetList itemList, Pair<Panel, Integer> currentPos, int numcolumns, int labelWidth, int spacing, int slot,
                                               boolean newgroup, String groupName) {
        Panel panel = currentPos.getKey();
        if (panel == null || currentPos.getValue() >= numcolumns || (newgroup && groupName != null)) {
            if (newgroup && groupName != null) {
                AbstractWidget<?> groupLabel = label(groupName).color(ModularStorageConfiguration.groupForeground.get())
                        .color(StyleConfig.colorTextInListNormal)
                        .horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).filledBackground(ModularStorageConfiguration.groupBackground.get()).desiredHeight(10)
                        .desiredWidth(231);
                itemList.children(new Panel().layout(new HorizontalLayout().setHorizontalMargin(2).setVerticalMargin(0)).desiredHeight(10).children(groupLabel));
            }

            panel = horizontal(DEFAULT_HORIZONTAL_MARGIN, spacing).desiredHeight(12).userObject(-1).desiredHeight(16);
            currentPos = MutablePair.of(panel, 0);
            itemList.children(panel);
        }
        BlockRender blockRender = new BlockRender().renderItem(stack).userObject(slot).offsetX(-1).offsetY(-1);
        panel.children(blockRender);
        if (labelWidth > 0) {
            String displayName;
            if (labelWidth > 100) {
                displayName = typeModule.getLongLabel(stack);
            } else {
                displayName = typeModule.getShortLabel(stack);
            }
            AbstractWidget<?> label = label(displayName).color(StyleConfig.colorTextInListNormal).horizontalAlignment(HorizontalAlignment.ALIGN_LEFT).desiredWidth(labelWidth).userObject(new Integer(-1));
            panel.children(label);
        }
        currentPos.setValue(currentPos.getValue() + 1);
        return currentPos;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean rc = false;
        if (!window.keyTyped(keyCode, scanCode)) {
            if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_9) {
                return true;
            }
            rc = super.keyPressed(keyCode, scanCode, modifiers);
        }

        // @todo 1.14 rc handling is probably not right
//        craftingGrid.getWindow().keyTyped(keyCode, keyCode);
        return rc;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float v, int i, int i2) {
        updateList();

        if (tileEntity != null) {
            viewMode.setCurrentChoice(tileEntity.getViewMode());
            sortMode.setCurrentChoice(tileEntity.getSortMode());
            groupMode.setCurrentChoice(tileEntity.isGroupMode() ? 1 : 0);
            String curFilter = tileEntity.getFilter();
            if (!this.filter.getText().equals(curFilter)) {
                this.filter.text(curFilter);
            }
        }

        drawWindow(matrixStack);
    }

    @Override
    protected void renderHoveredTooltip(MatrixStack matrixStack, int x, int y) {
        Slot slot = getSelectedSlot(x, y);
        if (slot instanceof SlotItemHandler && !(slot instanceof BaseSlot) && !(slot instanceof GhostOutputSlot) && !(slot instanceof GhostSlot)) {
            if (tileEntity.isLocked()) {
                renderTooltip(matrixStack, new StringTextComponent("Unlock to access these slots").mergeStyle(TextFormatting.RED), x, y);
                return;
            }
        }
        super.renderHoveredTooltip(matrixStack, x, y);
    }

    @Override
    protected void drawStackTooltips(MatrixStack matrixStack, int mouseX, int mouseY) {
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        int x = GuiTools.getRelativeX(this);
        int y = GuiTools.getRelativeY(this);

        List<String> tooltips = craftingGrid.getWindow().getTooltips();
        if (tooltips != null) {
            drawHoveringText(matrixStack, tooltips, window.getTooltipItems(), x - guiLeft, y - guiTop, minecraft.fontRenderer);
        }

        if (tileEntity.isLocked()) {
            minecraft.getTextureManager().bindTexture(guiElements);

            int offset = 300;
            blit(matrixStack, 5, ySize-79, offset, 96, 96, 16, 16, 256, 256);
            blit(matrixStack, 5, ySize-61, offset, 96, 96, 16, 16, 256, 256);
        }

        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);

        warningLabel.visible(!tileEntity.isLocked());
        itemList.visible(tileEntity.isLocked());
        lockButton.pressed(tileEntity.isLocked());
    }



    @Override
    protected void drawWindow(MatrixStack matrixStack) {
        super.drawWindow(matrixStack);
        craftingGrid.draw(matrixStack);
    }
}
