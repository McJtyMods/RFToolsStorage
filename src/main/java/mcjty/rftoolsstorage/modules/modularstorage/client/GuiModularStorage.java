package mcjty.rftoolsstorage.modules.modularstorage.client;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.container.BaseSlot;
import mcjty.lib.container.GhostOutputSlot;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbase.RFToolsBase;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageConfiguration;
import mcjty.rftoolsstorage.craftinggrid.CraftingGridProvider;
import mcjty.rftoolsstorage.craftinggrid.GuiCraftingGrid;
import mcjty.rftoolsstorage.modules.modularstorage.items.StorageModuleItem;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageTileEntity;
import mcjty.rftoolsstorage.network.RFToolsStorageMessages;
import mcjty.rftoolsstorage.setup.CommandHandler;
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
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer.CONTAINER_GRID;
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
    private Button cycleButton;
    private Button compactButton;

    private GuiCraftingGrid craftingGrid;

    // @todo 1.14
//    public GuiModularStorage(ModularStorageTileEntity modularStorageTileEntity, ModularStorageContainer container) {
//        this(modularStorageTileEntity, (Container) container);
//    }
//    public GuiModularStorage(RemoteStorageItemContainer container) {
//        this(null, container);
//    }
//
//    public GuiModularStorage(ModularStorageItemContainer container) {
//        this(null, container);
//    }

    public GuiModularStorage(ModularStorageTileEntity tileEntity, ModularStorageContainer container, PlayerInventory inventory) {
        super(RFToolsStorage.instance, RFToolsStorageMessages.INSTANCE, tileEntity, container, inventory, 0, "storage");

        craftingGrid = new GuiCraftingGrid();

        xSize = STORAGE_WIDTH;

//        double sw = Minecraft.getInstance().mainWindow.getScaledWidth();
        double height = Minecraft.getInstance().mainWindow.getScaledHeight();

        if (height > 510) {
            ySize = ModularStorageConfiguration.height3.get();
        } else if (height > 340) {
            ySize = ModularStorageConfiguration.height2.get();
        } else {
            ySize = ModularStorageConfiguration.height1.get();
        }

        IItemHandler gridInventory = container.getInventory(CONTAINER_GRID);
        for (Object o : container.inventorySlots) {
            SlotItemHandler slot = (SlotItemHandler) o;
            if (slot.getItemHandler() != gridInventory) {
                slot.yPos = slot.yPos + ySize - ModularStorageConfiguration.height1.get();
                //                slot.yPos += ySize - STORAGE_HEIGHT0;
            }
        }
    }

    @Override
    public void init() {
        super.init();

        itemList = new WidgetList(minecraft, this).setName("items").setLayoutHint(new PositionalLayout.PositionalHint(5, 3, 235, ySize - 89)).setNoSelectionMode(true).setUserObject(new Integer(-1)).
                setLeftMargin(0).setRowheight(-1);
        Slider slider = new Slider(minecraft, this).setLayoutHint(new PositionalLayout.PositionalHint(241, 3, 11, ySize - 89)).setDesiredWidth(11).setVertical()
                .setScrollableName("items");


        Panel modePanel = setupModePanel();

        cycleButton = new Button(minecraft, this)
                .setName("cycle")
                .setChannel("cycle")
                .setText("C").setTooltips("Cycle to the next storage module").setLayoutHint(new PositionalLayout.PositionalHint(5, ySize - 23, 16, 16));

        Panel toplevel = new Panel(minecraft, this).setLayout(new PositionalLayout()).addChild(itemList).addChild(slider)
                .addChild(modePanel)
                .addChild(cycleButton);

        toplevel.setBackgrounds(iconLocationTop, iconLocation);
        toplevel.setBackgroundLayout(false, ySize - ModularStorageConfiguration.height1.get() + 2);

        if (tileEntity == null) {
            // We must hide three slots.
            ImageLabel hideLabel = new ImageLabel(minecraft, this);
            hideLabel.setLayoutHint(new PositionalLayout.PositionalHint(4, ySize - 26 - 3 * 18, 20, 55));
            hideLabel.setImage(guiElements, 32, 32);
            toplevel.addChild(hideLabel);
        }

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

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

        craftingGrid.initGui(modBase, network, minecraft, this, pos, provider, guiLeft, guiTop, xSize, ySize);
        sendServerCommand(RFToolsStorage.MODID, CommandHandler.CMD_REQUEST_GRID_SYNC, TypedMap.builder().put(CommandHandler.PARAM_POS, pos).build());
    }

    private Panel setupModePanel() {
        filter = new TextField(minecraft, this).setLayoutHint(3, 3, 57, 13).setTooltips("Name based filter for items")
                .addTextEvent((parent, newText) -> updateSettings());

        viewMode = new ImageChoiceLabel(minecraft, this).setLayoutHint(4, 19, 16, 16).setTooltips("Control how items are shown", "in the view")
                .addChoiceEvent((parent, newChoice) -> updateSettings());
        viewMode.addChoice(VIEW_LIST, "Items are shown in a list view", guiElements, 9 * 16, 16);
        viewMode.addChoice(VIEW_COLUMNS, "Items are shown in columns", guiElements, 10 * 16, 16);
        viewMode.addChoice(VIEW_ICONS, "Items are shown with icons", guiElements, 11 * 16, 16);

        updateTypeModule();

        sortMode = new ImageChoiceLabel(minecraft, this).setLayoutHint(23, 19, 16, 16).setTooltips("Control how items are sorted", "in the view")
                .addChoiceEvent((parent, newChoice) -> updateSettings());
        for (ItemSorter sorter : typeModule.getSorters()) {
            sortMode.addChoice(sorter.getName(), sorter.getTooltip(), guiElements, sorter.getU(), sorter.getV());
        }

        groupMode = new ImageChoiceLabel(minecraft, this).setLayoutHint(42, 19, 16, 16).setTooltips("If enabled it will show groups", "based on sorting criterium")
                .addChoiceEvent((parent, newChoice) -> updateSettings());
        groupMode.addChoice("Off", "Don't show groups", guiElements, 13 * 16, 0);
        groupMode.addChoice("On", "Show groups", guiElements, 14 * 16, 0);

        amountLabel = new Label(minecraft, this);
        amountLabel.setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
        amountLabel.setLayoutHint(16, 40, 66, 12);
        amountLabel.setTooltips("Amount of stacks / maximum amount");
        amountLabel.setText("?/?");

        compactButton = new Button(minecraft, this)
                .setName("compact")
                .setChannel("compact")
                .setLayoutHint(4, 39, 12, 12).setText("z").setTooltips("Compact equal stacks");

        if (tileEntity != null) {
            filter.setText(ModularStorageConfiguration.clearSearchOnOpen.get() ? "" : tileEntity.getFilter());
            setViewMode(tileEntity.getViewMode());
            setSortMode(tileEntity.getSortMode());
            groupMode.setCurrentChoice(tileEntity.isGroupMode() ? 1 : 0);
        } else {
            ItemStack heldItem = minecraft.player.getHeldItem(Hand.MAIN_HAND);
            if (!heldItem.isEmpty() && heldItem.hasTag()) {
                CompoundNBT tagCompound = heldItem.getTag();
                filter.setText(ModularStorageConfiguration.clearSearchOnOpen.get() ? "" : tagCompound.getString("filter"));
                setViewMode(tagCompound.getString("viewMode"));
                setSortMode(tagCompound.getString("sortMode"));
                groupMode.setCurrentChoice(tagCompound.getBoolean("groupMode") ? 1 : 0);
            }
        }

        return new Panel(minecraft, this).setLayout(new PositionalLayout()).setLayoutHint(new PositionalLayout.PositionalHint(24, ySize - 80, 64, 77))
                .setFilledRectThickness(-2)
                .setFilledBackground(StyleConfig.colorListBackground)
                .addChildren(filter, viewMode, sortMode, groupMode, amountLabel, compactButton);
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
            window.sendAction(RFToolsStorageMessages.INSTANCE, tileEntity, ModularStorageTileEntity.ACTION_CYCLE);
        } else {
            sendServerCommand(RFToolsStorage.MODID, CommandHandler.CMD_CYCLE_STORAGE);
        }
    }

    private void compact() {
        if (tileEntity != null) {
            window.sendAction(RFToolsStorageMessages.INSTANCE, tileEntity, ModularStorageTileEntity.ACTION_COMPACT);
        } else {
            sendServerCommand(RFToolsStorage.MODID, CommandHandler.CMD_COMPACT);
        }
    }

    private void updateSettings() {
        if (tileEntity != null) {
            tileEntity.setSortMode(sortMode.getCurrentChoice());
            tileEntity.setViewMode(viewMode.getCurrentChoice());
            tileEntity.setFilter(filter.getText());
            tileEntity.setGroupMode(groupMode.getCurrentChoiceIndex() == 1);
            sendServerCommand(RFToolsStorageMessages.INSTANCE, ModularStorageTileEntity.CMD_SETTINGS,
                    TypedMap.builder()
                            .put(PARAM_SORTMODE, sortMode.getCurrentChoice())
                            .put(PARAM_VIEWMODE, viewMode.getCurrentChoice())
                            .put(PARAM_FILTER, filter.getText())
                            .put(PARAM_GROUPMODE, groupMode.getCurrentChoiceIndex() == 1)
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
            Widget<?> widget = window.getToplevel().getWidgetAtPosition((int)x, (int)y);
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
        Widget<?> widget = window.getToplevel().getWidgetAtPosition((int)x, (int)y);
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
        craftingGrid.getWindow().mouseClicked((int)x, (int)y, button);
        if (button == 1) {
            Slot slot = getSelectedSlot(x, y);
            if (slot instanceof GhostOutputSlot) {
                if (tileEntity != null) {
                    window.sendAction(RFToolsStorageMessages.INSTANCE, tileEntity, ModularStorageTileEntity.ACTION_CLEARGRID);
                } else {
                    sendServerCommand(RFToolsStorage.MODID, CommandHandler.CMD_CLEAR_GRID);
                }
            }
        }
        return r;
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double scaledX, double scaledY) {
        craftingGrid.getWindow().handleMouseInput(button);  // @todo 1.14 check?
        return super.mouseDragged(x, y, button, scaledX, scaledY);
    }


    @Override
    public boolean mouseReleased(double x, double y, int state) {
        boolean rc = super.mouseReleased(x, y, state);
        craftingGrid.getWindow().mouseMovedOrUp((int)x, (int)y, state);
        return rc;
    }

    private void updateList() {
        itemList.removeChildren();

        if (tileEntity != null && !container.getSlot(ModularStorageContainer.SLOT_STORAGE_MODULE).getHasStack()) {
            amountLabel.setText("(empty)");
            compactButton.setEnabled(false);
            cycleButton.setEnabled(false);
            return;
        }

        cycleButton.setEnabled(isTabletWithRemote() || isRemote());

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
                        String displayName = stack.getDisplayName().getFormattedText();
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
        amountLabel.setText(items.size() + "/" + max);
        compactButton.setEnabled(max.get() > 0);

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
            sortMode.addChoice(sorter.getName(), sorter.getTooltip(), guiElements, sorter.getU(), sorter.getV());
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
                AbstractWidget<?> groupLabel = new Label(minecraft, this).setText(groupName).setColor(ModularStorageConfiguration.groupForeground.get())
                        .setColor(StyleConfig.colorTextInListNormal)
                        .setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT).setFilledBackground(ModularStorageConfiguration.groupBackground.get()).setDesiredHeight(10)
                        .setDesiredWidth(231);
                itemList.addChild(new Panel(minecraft, this).setLayout(new HorizontalLayout().setHorizontalMargin(2).setVerticalMargin(0)).setDesiredHeight(10).addChild(groupLabel));
            }

            panel = new Panel(minecraft, this).setLayout(new HorizontalLayout().setSpacing(spacing)).setDesiredHeight(12).setUserObject(new Integer(-1)).setDesiredHeight(16);
            currentPos = MutablePair.of(panel, 0);
            itemList.addChild(panel);
        }
        BlockRender blockRender = new BlockRender(minecraft, this).setRenderItem(stack).setUserObject(slot).setOffsetX(-1).setOffsetY(-1);
        panel.addChild(blockRender);
        if (labelWidth > 0) {
            String displayName;
            if (labelWidth > 100) {
                displayName = typeModule.getLongLabel(stack);
            } else {
                displayName = typeModule.getShortLabel(stack);
            }
            AbstractWidget<?> label = new Label(minecraft, this).setText(displayName).setColor(StyleConfig.colorTextInListNormal).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT).setDesiredWidth(labelWidth).setUserObject(new Integer(-1));
            panel.addChild(label);
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
        craftingGrid.getWindow().keyTyped(keyCode, keyCode);
        return rc;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        updateList();

        if (tileEntity != null) {
            viewMode.setCurrentChoice(tileEntity.getViewMode());
            sortMode.setCurrentChoice(tileEntity.getSortMode());
            groupMode.setCurrentChoice(tileEntity.isGroupMode() ? 1 : 0);
            String curFilter = tileEntity.getFilter();
            if (!this.filter.getText().equals(curFilter)) {
                this.filter.setText(curFilter);
            }
        }

        drawWindow();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i1, int i2) {
        double mouseX = minecraft.mouseHelper.getMouseX();
        double mouseY = minecraft.mouseHelper.getMouseY();
        int x = (int) (mouseX * width / minecraft.mainWindow.getWidth());
        int y = (int) (height - mouseY * height / minecraft.mainWindow.getHeight() - 1);

        List<String> tooltips = craftingGrid.getWindow().getTooltips();
        if (tooltips != null) {
            drawHoveringText(tooltips, window.getTooltipItems(), x - guiLeft, y - guiTop, minecraft.fontRenderer);
        }

        super.drawGuiContainerForegroundLayer(i1, i2);
    }



    @Override
    protected void drawWindow() {
        super.drawWindow();
        craftingGrid.draw();
    }
}
