package mcjty.rftoolsstorage.modules.scanner.client;

import mcjty.lib.McJtyLib;
import mcjty.lib.base.StyleConfig;
import mcjty.lib.container.GhostOutputSlot;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.GuiTools;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.BlockRenderEvent;
import mcjty.lib.gui.events.DefaultSelectionEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbase.RFToolsBase;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.craftinggrid.GuiCraftingGrid;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerConfiguration;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerContainer;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import mcjty.rftoolsstorage.modules.scanner.network.PacketGetInventoryInfo;
import mcjty.rftoolsstorage.modules.scanner.network.PacketRequestItem;
import mcjty.rftoolsstorage.modules.scanner.network.PacketReturnInventoryInfo;
import mcjty.rftoolsstorage.modules.scanner.tools.SortingMode;
import mcjty.rftoolsstorage.setup.CommandHandler;
import mcjty.rftoolsstorage.setup.RFToolsStorageMessages;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.energy.CapabilityEnergy;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Predicate;

import static mcjty.lib.gui.widgets.Widgets.*;
import static mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity.*;


public class GuiStorageScanner extends GenericGuiContainer<StorageScannerTileEntity, StorageScannerContainer> {
    private static final int STORAGE_MONITOR_WIDTH = 256;
    private static final int STORAGE_MONITOR_HEIGHT = 244;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFToolsStorage.MODID, "textures/gui/storagescanner.png");
    private static final ResourceLocation guielements = new ResourceLocation(RFToolsBase.MODID, "textures/gui/guielements.png");

    private WidgetList storageList;
    private WidgetList itemList;
    private ToggleButton openViewButton;
    private EnergyBar energyBar;
    private Button topButton;
    private Button upButton;
    private Button downButton;
    private Button bottomButton;
    private ChoiceLabel sortChoice;
    private Button removeButton;
    private TextField searchField;
    private ImageChoiceLabel exportToStarred;
    private Panel storagePanel;
    private Panel itemPanel;
    private ScrollableLabel radiusLabel;
    private Label visibleRadiusLabel;

    private GuiCraftingGrid craftingGrid;

    private long prevTime = -1;

    private int listDirty = 0;
    private boolean init = false;

    // From server: all the positions with inventories
    public static List<PacketReturnInventoryInfo.InventoryInfo> fromServer_inventories = new ArrayList<>();
    // From server: all the positions with inventories matching the search
    public static Set<BlockPos> fromServer_foundInventories = new HashSet<>();

    // From server: the contents of an inventory
    public static List<ItemStack> fromServer_inventory = new ArrayList<>();
    // From server: the contents of an inventory (craftables)
    public static List<ItemStack> fromServer_craftable = new ArrayList<>();

    public GuiStorageScanner(StorageScannerTileEntity te, StorageScannerContainer container, PlayerInventory playerInventory) {
        super(RFToolsStorage.instance, te, container, playerInventory, 0 /* @todo 1.14 */, "stomon");

        craftingGrid = new GuiCraftingGrid();

        xSize = STORAGE_MONITOR_WIDTH;
        ySize = STORAGE_MONITOR_HEIGHT;
    }

    @Override
    public void init() {
        super.init();

        energyBar = new EnergyBar().filledRectThickness(1).vertical().desiredWidth(10).desiredHeight(50).showText(false);

        openViewButton = new ToggleButton().checkMarker(false).text("V")
                .tooltips("Toggle wide storage list");
        openViewButton.pressed(tileEntity.isOpenWideView());
        openViewButton.event(this::toggleView);
        upButton = button("U").channel("up").tooltips("Move inventory up");
        topButton = button("T").channel("top").tooltips("Move inventory to the top");
        downButton = button("D").channel("down").tooltips("Move inventory down");
        bottomButton = button("B").channel("bottom").tooltips("Move inventory to the bottom");
        removeButton = button("R").channel("remove").tooltips("Remove inventory from list");

        Panel energyPanel = vertical(0, 1).desiredWidth(10);
        energyPanel.children(openViewButton, energyBar, topButton, upButton, downButton, bottomButton, label(" "), removeButton);

        exportToStarred = imageChoice(12, 223, 13, 13).name("export");
        exportToStarred.choice("No", "Export to current container", guielements, 131, 19);
        exportToStarred.choice("Yes", "Export to first routable container", guielements, 115, 19);

        storagePanel = makeStoragePanel(energyPanel);
        itemPanel = makeItemPanel();

        Button scanButton = button("Scan")
                .channel("scan")
                .desiredWidth(50)
                .desiredHeight(14);
        if (RFToolsStorage.setup.xnet) {
            if (StorageScannerConfiguration.xnetRequired.get()) {
                scanButton
                        .tooltips("Do a scan of all", "storage units connected", "with an active XNet channel");
            } else {
                scanButton
                        .tooltips("Do a scan of all", "storage units in radius", "Use 'xnet' radius to", "restrict to XNet only");
            }
        } else {
            scanButton
                    .tooltips("Do a scan of all", "storage units in radius");
        }
        radiusLabel = new ScrollableLabel()
                .hint(1, 1, 1, 1)
                .name("radius")
                .visible(false)
                .realMinimum(RFToolsStorage.setup.xnet ? 0 : 1)
                .realMaximum(20);
        visibleRadiusLabel = new Label().desiredWidth(40);

        sortChoice = new ChoiceLabel().tooltips("Sort the items in the list").name("sortMode")
            .desiredWidth(60);
        for (SortingMode mode : SortingMode.values()) {
            sortChoice.choices(mode.getDescription());
        }
        sortChoice.choice(tileEntity.getSortingMode().getDescription());

        searchField = new TextField().event((newText) -> {
            storageList.clearHilightedRows();
            fromServer_foundInventories.clear();
            startSearch(newText);
        });
        Panel searchPanel = horizontal()
                .hint(8, 142, 256 - 11, 18)
                .desiredHeight(18)
                .children(sortChoice, label("Search:"), searchField);

        Slider radiusSlider = new Slider()
                .horizontal()
                .tooltips("Radius of scan")
                .minimumKnobSize(12)
                .desiredHeight(14)
                .scrollableName("radius");
        Panel scanPanel = vertical(6, 1)
                .hint(8, 162, 74, 54)
                .filledRectThickness(-2)
                .filledBackground(StyleConfig.colorListBackground)
                .children(scanButton);
        if (!(RFToolsStorage.setup.xnet && StorageScannerConfiguration.xnetRequired.get())) {
            scanPanel.children(radiusSlider);
        }
        scanPanel.children(visibleRadiusLabel, radiusLabel);

        if (tileEntity.isDummy()) {
            scanButton.enabled(false);
            radiusSlider.visible(false);
        }

        Panel toplevel = positional().background(iconLocation)
                .children(storagePanel, itemPanel, searchPanel, scanPanel, exportToStarred);
        toplevel.bounds(guiLeft, guiTop, xSize, ySize);

        window = new Window(this, toplevel);

        window.bind(RFToolsStorageMessages.INSTANCE, "export", tileEntity, StorageScannerTileEntity.VALUE_EXPORT.getName());
        window.bind(RFToolsStorageMessages.INSTANCE, "radius", tileEntity, StorageScannerTileEntity.VALUE_RADIUS.getName());
        window.bind(RFToolsStorageMessages.INSTANCE, "sortMode", tileEntity, StorageScannerTileEntity.VALUE_SORTMODE.getName());
        window.event("up", (source, params) -> moveUp());
        window.event("top", (source, params) -> moveTop());
        window.event("down", (source, params) -> moveDown());
        window.event("bottom", (source, params) -> moveBottom());
        window.event("remove", (source, params) -> removeFromList());
        window.event("scan", (source, params) -> RFToolsStorageMessages.INSTANCE.sendToServer(new PacketGetInventoryInfo(tileEntity.getDimension(), tileEntity.getStorageScannerPos(), true)));

        minecraft.keyboardListener.enableRepeatEvents(true);

        fromServer_foundInventories.clear();
        fromServer_inventory.clear();
        fromServer_craftable.clear();

        if (tileEntity.isDummy()) {
            fromServer_inventories.clear();
        }

        BlockPos pos = tileEntity.getCraftingGridContainerPos();
        craftingGrid.initGui(modBase, RFToolsStorageMessages.INSTANCE, minecraft, this, pos, tileEntity.getCraftingGridProvider(), guiLeft, guiTop, xSize, ySize);
        sendServerCommand(RFToolsStorageMessages.INSTANCE, RFToolsStorage.MODID, CommandHandler.CMD_REQUEST_GRID_SYNC, TypedMap.builder().put(CommandHandler.PARAM_POS, pos).build());

        if (StorageScannerConfiguration.hilightStarredOnGuiOpen.get()) {
            storageList.selected(0);
        }

        init = true;
    }

    private int getStoragePanelWidth() {
        return openViewButton.isPressed() ? 130 : 50;
    }

    private Panel makeItemPanel() {
        itemList = new WidgetList().name("items").propagateEventsToChildren(true)
                .invisibleSelection(true);
        Slider itemListSlider = new Slider().desiredWidth(10).vertical().scrollableName("items");
        return horizontal(1, 1)
                .hint(getStoragePanelWidth() + 6, 4, 256 - getStoragePanelWidth() - 12, 86 + 54)
                .children(itemList, itemListSlider);
    }

    private Panel makeStoragePanel(Panel energyPanel) {
        storageList = new WidgetList().name("storage").event(new DefaultSelectionEvent() {
            @Override
            public void select(int index) {
                getInventoryOnServer();
            }

            @Override
            public void doubleClick(int index) {
                hilightSelectedContainer(index);
            }
        }).propagateEventsToChildren(true);

        Slider storageListSlider = new Slider().desiredWidth(10).vertical().scrollableName("storage");

        return horizontal(1, 1)
                .hint(3, 4, getStoragePanelWidth(), 86 + 54)
                .desiredHeight(86 + 54)
                .children(energyPanel, storageList, storageListSlider);
    }

    private void toggleView() {
        storagePanel.hint(3, 4, getStoragePanelWidth(), 86 + 54);
        itemPanel.hint(getStoragePanelWidth() + 6, 4, 256 - getStoragePanelWidth() - 12, 86 + 54);
        window.getToplevel().markLayoutDirty();
        listDirty = 0;
        requestListsIfNeeded();
        sendServerCommandTyped(RFToolsStorageMessages.INSTANCE, tileEntity.getDimension(), StorageScannerTileEntity.CMD_SETVIEW,
                TypedMap.builder()
                        .put(PARAM_VIEW, openViewButton.isPressed())
                        .build());
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        boolean r = super.mouseClicked(x, y, button);
        craftingGrid.getWindow().mouseClicked(x, y, button);
        if (button == 1) {
            Slot slot = getSelectedSlot(x, y);
            if (slot instanceof GhostOutputSlot) {
                window.sendAction(RFToolsStorageMessages.INSTANCE, tileEntity, StorageScannerTileEntity.ACTION_CLEARGRID);
            }
        }
        return r;
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double scaledX, double scaledY) {
        craftingGrid.getWindow().mouseDragged(x, y, button);
        return super.mouseDragged(x, y, button, scaledX, scaledY);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double amount) {
        craftingGrid.getWindow().mouseScrolled(x, y, amount);
        return super.mouseScrolled(x, y, amount);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        boolean rc = super.mouseReleased(x, y, button);
        craftingGrid.getWindow().mouseReleased(x, y, button);
        return rc;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean rc = super.keyPressed(keyCode, scanCode, modifiers);
        // @todo 1.14 rc handling is probably not right
        craftingGrid.getWindow().keyTyped(keyCode, keyCode);
        return rc;
    }


    private void moveUp() {
        sendServerCommandTyped(RFToolsStorageMessages.INSTANCE, tileEntity.getDimension(), StorageScannerTileEntity.CMD_UP,
                TypedMap.builder().put(PARAM_INDEX, storageList.getSelected() - 1).build());
        storageList.selected(storageList.getSelected() - 1);
        listDirty = 0;
    }

    private void moveTop() {
        sendServerCommandTyped(RFToolsStorageMessages.INSTANCE, tileEntity.getDimension(), StorageScannerTileEntity.CMD_TOP,
                TypedMap.builder().put(PARAM_INDEX, storageList.getSelected() - 1).build());
        storageList.selected(1);
        listDirty = 0;
    }

    private void moveDown() {
        sendServerCommandTyped(RFToolsStorageMessages.INSTANCE, tileEntity.getDimension(), StorageScannerTileEntity.CMD_DOWN,
                TypedMap.builder().put(PARAM_INDEX, storageList.getSelected() - 1).build());
        storageList.selected(storageList.getSelected() + 1);
        listDirty = 0;
    }

    private void moveBottom() {
        sendServerCommandTyped(RFToolsStorageMessages.INSTANCE, tileEntity.getDimension(), StorageScannerTileEntity.CMD_BOTTOM,
                TypedMap.builder().put(PARAM_INDEX, storageList.getSelected() - 1).build());
        storageList.selected(storageList.getChildCount() - 1);
        listDirty = 0;
    }

    private void removeFromList() {
        sendServerCommandTyped(RFToolsStorageMessages.INSTANCE, tileEntity.getDimension(), StorageScannerTileEntity.CMD_REMOVE,
                TypedMap.builder().put(PARAM_INDEX, storageList.getSelected() - 1).build());
        listDirty = 0;
    }

    private void hilightSelectedContainer(int index) {
        if (index == -1) {
            return;
        }
        if (index == 0) {
            // Starred
            return;
        }
        PacketReturnInventoryInfo.InventoryInfo c = fromServer_inventories.get(index - 1);
        if (c != null) {
            // @todo 1.14
//            RFTools.instance.clientInfo.hilightBlock(c.getPos(), System.currentTimeMillis() + 1000 * StorageScannerConfiguration.hilightTime.get());
            Logging.message(minecraft.player, "The inventory is now highlighted");
            minecraft.player.closeScreen();
        }
    }

    private void startSearch(String text) {
        if (!text.isEmpty()) {
            sendServerCommand(RFToolsStorageMessages.INSTANCE, RFToolsStorage.MODID, CommandHandler.CMD_SCANNER_SEARCH,
                    TypedMap.builder()
                            .put(CommandHandler.PARAM_SCANNER_DIM, tileEntity.getDimension().getId())
                            .put(CommandHandler.PARAM_SCANNER_POS, tileEntity.getStorageScannerPos())
                            .put(CommandHandler.PARAM_SEARCH_TEXT, text)
                            .build());
        }
    }

    private void getInventoryOnServer() {
        BlockPos c = getSelectedContainerPos();
        if (c != null) {
            sendServerCommand(RFToolsStorageMessages.INSTANCE, RFToolsStorage.MODID, CommandHandler.CMD_REQUEST_SCANNER_CONTENTS,
                    TypedMap.builder()
                            .put(CommandHandler.PARAM_SCANNER_DIM, tileEntity.getDimension().getId())
                            .put(CommandHandler.PARAM_SCANNER_POS, tileEntity.getStorageScannerPos())
                            .put(CommandHandler.PARAM_INV_POS, c)
                            .build());
        }
    }

    private BlockPos getSelectedContainerPos() {
        int selected = storageList.getSelected();
        if (selected != -1) {
            if (selected == 0) {
                return new BlockPos(-1, -1, -1);
            }
            selected--;
            if (selected < fromServer_inventories.size()) {
                PacketReturnInventoryInfo.InventoryInfo info = fromServer_inventories.get(selected);
                if (info == null) {
                    return null;
                } else {
                    return info.getPos();
                }
            }
        }
        return null;
    }

    private void requestListsIfNeeded() {
        listDirty--;
        if (listDirty <= 0) {
            RFToolsStorageMessages.INSTANCE.sendToServer(new PacketGetInventoryInfo(tileEntity.getDimension(), tileEntity.getStorageScannerPos(), false));
            getInventoryOnServer();
            listDirty = 20;
        }
    }

    private void updateContentsList() {
        itemList.removeChildren();

        Pair<Panel, Integer> currentPos = MutablePair.of(null, 0);
        int numcolumns = openViewButton.isPressed() ? 5 : 9;
        int spacing = 3;

        switch (tileEntity.getSortingMode()) {
            case AMOUNT_ASCENDING:
                Collections.sort(fromServer_inventory, Comparator.comparing(ItemStack::getCount));
                Collections.sort(fromServer_craftable, Comparator.comparing(ItemStack::getCount));
                break;
            case AMOUNT_DESCENDING:
                Collections.sort(fromServer_inventory, Comparator.comparing(ItemStack::getCount).reversed());
                Collections.sort(fromServer_craftable, Comparator.comparing(ItemStack::getCount).reversed());
                break;
            case NAME:
                Collections.sort(fromServer_inventory, Comparator.comparing(itemStack -> itemStack.getDisplayName().getFormattedText()));
                Collections.sort(fromServer_craftable, Comparator.comparing(itemStack -> itemStack.getDisplayName().getFormattedText()));
                break;
        }

        String filterText = searchField.getText().toLowerCase();
        Predicate<ItemStack> matcher = StorageScannerTileEntity.getMatcher(filterText);

        for (ItemStack item : fromServer_inventory) {
            if (filterText.isEmpty() || matcher.test(item)) {
                currentPos = addItemToList(item, itemList, currentPos, numcolumns, spacing, false);
            }
        }
        for (ItemStack item : fromServer_craftable) {
            if (filterText.isEmpty() || matcher.test(item)) {
                // @todo
                currentPos = addItemToList(item, itemList, currentPos, numcolumns, spacing, true);
            }
        }
    }

    private Pair<Panel, Integer> addItemToList(ItemStack item, WidgetList itemList, Pair<Panel, Integer> currentPos, int numcolumns, int spacing, boolean craftable) {
        Panel panel = currentPos.getKey();
        if (panel == null || currentPos.getValue() >= numcolumns) {
            panel = horizontal(1, spacing)
                    .desiredHeight(12).userObject(new Integer(-1)).desiredHeight(16);
            currentPos = MutablePair.of(panel, 0);
            itemList.children(panel);
        }
        BlockRender blockRender = new BlockRender()
                .renderItem(item)
                .userObject(1)       // Mark as a special stack in the renderer (for tooltip)
                .offsetX(-1)
                .offsetY(-1)
                .hilightOnHover(true);
        if (craftable) {
            // @todo is this looking nice?
            blockRender.filledBackground(0xffaaaa00);
        }
        blockRender.event(new BlockRenderEvent() {
            @Override
            public void select() {
                Object item = blockRender.getRenderItem();
                if (item != null) {
                    boolean shift = McJtyLib.proxy.isShiftKeyDown();
                    requestItem((ItemStack) item, shift ? 1 : -1, craftable);
                }
            }

            @Override
            public void doubleClick() {
            }
        });
        panel.children(blockRender);
        currentPos.setValue(currentPos.getValue() + 1);
        return currentPos;
    }

    private void requestItem(ItemStack stack, int amount, boolean craftable) {
        BlockPos selectedContainerPos = getSelectedContainerPos();
        if (selectedContainerPos == null) {
            return;
        }
        RFToolsStorageMessages.INSTANCE.sendToServer(new PacketRequestItem(tileEntity.getDimension(), tileEntity.getStorageScannerPos(), selectedContainerPos, stack, amount, craftable));
        getInventoryOnServer();
    }

    private void changeRoutable(BlockPos c) {
        sendServerCommandTyped(RFToolsStorageMessages.INSTANCE, tileEntity.getDimension(), StorageScannerTileEntity.CMD_TOGGLEROUTABLE,
                TypedMap.builder().put(PARAM_POS, c).build());
        listDirty = 0;
    }

    private void updateStorageList() {
        storageList.removeChildren();
        addStorageLine(null, "All routable", false);
        for (PacketReturnInventoryInfo.InventoryInfo c : fromServer_inventories) {
            String displayName = c.getName();
            boolean routable = c.isRoutable();
            addStorageLine(c, displayName, routable);
        }


        storageList.clearHilightedRows();
        int i = 0;
        for (PacketReturnInventoryInfo.InventoryInfo c : fromServer_inventories) {
            if (fromServer_foundInventories.contains(c.getPos())) {
                storageList.addHilightedRow(i + 1);
            }
            i++;
        }
    }

    private void addStorageLine(PacketReturnInventoryInfo.InventoryInfo c, String displayName, boolean routable) {
        Panel panel;
        if (c == null) {
            panel = horizontal(5, 8);
            panel.children(new ImageLabel().image(guielements, 115, 19).desiredWidth(13).desiredHeight(13));
        } else {
            HorizontalLayout layout = new HorizontalLayout();
            if (!openViewButton.isPressed()) {
                layout.setHorizontalMargin(2);
            }
            panel = new Panel().layout(layout);
            panel.children(new BlockRender().renderItem(c.getBlock()));
        }
        if (openViewButton.isPressed()) {
            AbstractWidget<?> label;
            label = label(displayName)
                    .color(StyleConfig.colorTextInListNormal)
                    .dynamic(true)
                    .horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                    .desiredWidth(58);
            if (c == null) {
                label.tooltips(TextFormatting.GREEN + "All routable inventories")
                        .desiredWidth(74);
            } else {
                label.tooltips(TextFormatting.GREEN + "Block at: " + TextFormatting.WHITE + BlockPosTools.toString(c.getPos()),
                        TextFormatting.GREEN + "Name: " + TextFormatting.WHITE + displayName,
                        "(doubleclick to highlight)");
            }
            panel.children(label);
            if (c != null) {
                ImageChoiceLabel choiceLabel = new ImageChoiceLabel()
                        .event((newChoice) -> changeRoutable(c.getPos())).desiredWidth(13);
                choiceLabel.choice("No", "Not routable", guielements, 131, 19);
                choiceLabel.choice("Yes", "Routable", guielements, 115, 19);
                choiceLabel.setCurrentChoice(routable ? 1 : 0);
                panel.children(choiceLabel);
            }
        }
        storageList.children(panel);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        if (!init) {
            return;
        }
        updateStorageList();
        updateContentsList();
        requestListsIfNeeded();

        String text = radiusLabel.getText();
        if ("0".equals(text)) {
            text = "XNet";
        }
        visibleRadiusLabel.text(text);

        int selected = storageList.getSelected();
        removeButton.enabled(selected != -1);
        if (selected <= 0 || storageList.getChildCount() <= 2) {
            upButton.enabled(false);
            downButton.enabled(false);
            topButton.enabled(false);
            bottomButton.enabled(false);
        } else if (selected == 1) {
            topButton.enabled(false);
            upButton.enabled(false);
            downButton.enabled(true);
            bottomButton.enabled(true);
        } else if (selected == storageList.getChildCount() - 1) {
            topButton.enabled(true);
            upButton.enabled(true);
            downButton.enabled(false);
            bottomButton.enabled(false);
        } else {
            topButton.enabled(true);
            upButton.enabled(true);
            downButton.enabled(true);
            bottomButton.enabled(true);
        }

        if (!tileEntity.isDummy()) {
            tileEntity.getCapability(CapabilityEnergy.ENERGY).ifPresent(e -> {
                energyBar.maxValue(((GenericEnergyStorage)e).getCapacity());
                energyBar.value(((GenericEnergyStorage)e).getEnergy());
            });
            exportToStarred.setCurrentChoice(tileEntity.isExportToCurrent() ? 0 : 1);
        } else {
            if (System.currentTimeMillis() - lastTime > 300) {
                lastTime = System.currentTimeMillis();
                tileEntity.requestDataFromServer(RFToolsStorageMessages.INSTANCE, StorageScannerTileEntity.CMD_SCANNER_INFO, TypedMap.EMPTY);
            }
            energyBar.value(rfReceived);
            exportToStarred.setCurrentChoice(exportToCurrentReceived ? 0 : 1);
        }

        drawWindow();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i1, int i2) {
        if (!init) {
            return;
        }
        int x = GuiTools.getRelativeX(this);
        int y = GuiTools.getRelativeY(this);

        List<String> tooltips = craftingGrid.getWindow().getTooltips();
        if (tooltips != null) {
            drawHoveringText(tooltips, window.getTooltipItems(), x - guiLeft, y - guiTop, minecraft.fontRenderer);
        }

        super.drawGuiContainerForegroundLayer(i1, i2);
    }

    @Override
    protected void drawStackTooltips(int mouseX, int mouseY) {
        if (init) {
            super.drawStackTooltips(mouseX, mouseY);
        }
    }

    @Override
    protected List<String> addCustomLines(List<String> oldList, BlockRender blockRender, ItemStack stack) {
        if (blockRender.getUserObject() instanceof Integer) {
            List<String> newlist = new ArrayList<>();
            newlist.add(TextFormatting.GREEN + "Click: " + TextFormatting.WHITE + "full stack");
            newlist.add(TextFormatting.GREEN + "Shift + click: " + TextFormatting.WHITE + "single item");
            newlist.add("");
            newlist.addAll(oldList);
            return newlist;
        } else {
            return oldList;
        }
    }

    private static long lastTime = 0;

    @Override
    protected void drawWindow() {
        if (!init) {
            return;
        }
        super.drawWindow();
        craftingGrid.draw();
    }

}
