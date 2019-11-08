package mcjty.rftoolsstorage.modules.scanner.client;

import mcjty.lib.McJtyLib;
import mcjty.lib.base.StyleConfig;
import mcjty.lib.container.GhostOutputSlot;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.BlockRenderEvent;
import mcjty.lib.gui.events.DefaultSelectionEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.craftinggrid.GuiCraftingGrid;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerConfiguration;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerContainer;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import mcjty.rftoolsstorage.modules.scanner.network.PacketGetInventoryInfo;
import mcjty.rftoolsstorage.modules.scanner.network.PacketRequestItem;
import mcjty.rftoolsstorage.modules.scanner.network.PacketReturnInventoryInfo;
import mcjty.rftoolsstorage.network.RFToolsStorageMessages;
import mcjty.rftoolsstorage.setup.CommandHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.energy.CapabilityEnergy;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Predicate;

import static mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity.*;


public class GuiStorageScanner extends GenericGuiContainer<StorageScannerTileEntity, StorageScannerContainer> {
    private static final int STORAGE_MONITOR_WIDTH = 256;
    private static final int STORAGE_MONITOR_HEIGHT = 244;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFToolsStorage.MODID, "textures/gui/storagescanner.png");
    private static final ResourceLocation guielements = new ResourceLocation(RFToolsStorage.MODID, "textures/gui/guielements.png");

    private WidgetList storageList;
    private WidgetList itemList;
    private ToggleButton openViewButton;
    private EnergyBar energyBar;
    private Button topButton;
    private Button upButton;
    private Button downButton;
    private Button bottomButton;
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

    public GuiStorageScanner(StorageScannerTileEntity te, StorageScannerContainer container, PlayerInventory playerInventory) {
        super(RFToolsStorage.instance, RFToolsStorageMessages.INSTANCE, te, container, playerInventory, 0 /* @todo 1.14 */, "stomon");

        craftingGrid = new GuiCraftingGrid();

        xSize = STORAGE_MONITOR_WIDTH;
        ySize = STORAGE_MONITOR_HEIGHT;
    }

    @Override
    public void init() {
        super.init();

        energyBar = new EnergyBar(minecraft, this).setFilledRectThickness(1).setVertical().setDesiredWidth(10).setDesiredHeight(50).setShowText(false);

        openViewButton = new ToggleButton(minecraft, this).setCheckMarker(false).setText("V")
                .setTooltips("Toggle wide storage list");
        openViewButton.setPressed(tileEntity.isOpenWideView());
        openViewButton.addButtonEvent(widget -> toggleView());
        upButton = new Button(minecraft, this).setChannel("up").setText("U").setTooltips("Move inventory up");
        topButton = new Button(minecraft, this).setChannel("top").setText("T").setTooltips("Move inventory to the top");
        downButton = new Button(minecraft, this).setChannel("down").setText("D").setTooltips("Move inventory down");
        bottomButton = new Button(minecraft, this).setChannel("bottom").setText("B").setTooltips("Move inventory to the bottom");
        removeButton = new Button(minecraft, this).setChannel("remove").setText("R").setTooltips("Remove inventory from list");

        Panel energyPanel = new Panel(minecraft, this).setLayout(new VerticalLayout().setVerticalMargin(0).setSpacing(1))
                .setDesiredWidth(10);
        energyPanel
                .addChild(openViewButton)
                .addChild(energyBar)
                .addChild(topButton)
                .addChild(upButton)
                .addChild(downButton)
                .addChild(bottomButton)
                .addChild(new Label(minecraft, this).setText(" "))
                .addChild(removeButton);

        exportToStarred = new ImageChoiceLabel(minecraft, this)
                .setName("export")
                .setLayoutHint(12, 223, 13, 13);
        exportToStarred.addChoice("No", "Export to current container", guielements, 131, 19);
        exportToStarred.addChoice("Yes", "Export to first routable container", guielements, 115, 19);

        storagePanel = makeStoragePanel(energyPanel);
        itemPanel = makeItemPanel();

        Button scanButton = new Button(minecraft, this)
                .setChannel("scan")
                .setText("Scan")
                .setDesiredWidth(50)
                .setDesiredHeight(14);
        if (RFToolsStorage.setup.xnet) {
            if (StorageScannerConfiguration.xnetRequired.get()) {
                scanButton
                        .setTooltips("Do a scan of all", "storage units connected", "with an active XNet channel");
            } else {
                scanButton
                        .setTooltips("Do a scan of all", "storage units in radius", "Use 'xnet' radius to", "restrict to XNet only");
            }
        } else {
            scanButton
                    .setTooltips("Do a scan of all", "storage units in radius");
        }
        radiusLabel = new ScrollableLabel(minecraft, this)
                .setLayoutHint(1, 1, 1, 1)
                .setName("radius")
                .setVisible(false)
                .setRealMinimum(RFToolsStorage.setup.xnet ? 0 : 1)
                .setRealMaximum(20);
        visibleRadiusLabel = new Label(minecraft, this);
        visibleRadiusLabel.setDesiredWidth(40);

        searchField = new TextField(minecraft, this).addTextEvent((parent, newText) -> {
            storageList.clearHilightedRows();
            fromServer_foundInventories.clear();
            startSearch(newText);
        });
        Panel searchPanel = new Panel(minecraft, this)
                .setLayoutHint(new PositionalLayout.PositionalHint(8, 142, 256 - 11, 18))
                .setLayout(new HorizontalLayout()).setDesiredHeight(18)
                .addChild(new Label(minecraft, this).setText("Search:"))
                .addChild(searchField);

        Slider radiusSlider = new Slider(minecraft, this)
                .setHorizontal()
                .setTooltips("Radius of scan")
                .setMinimumKnobSize(12)
                .setDesiredHeight(14)
                .setScrollableName("radius");
        Panel scanPanel = new Panel(minecraft, this)
                .setLayoutHint(8, 162, 74, 54)
                .setFilledRectThickness(-2)
                .setFilledBackground(StyleConfig.colorListBackground)
                .setLayout(new VerticalLayout().setVerticalMargin(6).setSpacing(1))
                .addChild(scanButton);
        if (!(RFToolsStorage.setup.xnet && StorageScannerConfiguration.xnetRequired.get())) {
            scanPanel.addChild(radiusSlider);
        }
        scanPanel.addChildren(visibleRadiusLabel, radiusLabel);

        if (tileEntity.isDummy()) {
            scanButton.setEnabled(false);
            radiusSlider.setVisible(false);
        }

        Panel toplevel = new Panel(minecraft, this).setBackground(iconLocation).setLayout(new PositionalLayout())
                .addChild(storagePanel)
                .addChild(itemPanel)
                .addChild(searchPanel)
                .addChild(scanPanel)
                .addChild(exportToStarred);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);

        window.bind(RFToolsStorageMessages.INSTANCE, "export", tileEntity, StorageScannerTileEntity.VALUE_EXPORT.getName());
        window.bind(RFToolsStorageMessages.INSTANCE, "radius", tileEntity, StorageScannerTileEntity.VALUE_RADIUS.getName());
        window.event("up", (source, params) -> moveUp());
        window.event("top", (source, params) -> moveTop());
        window.event("down", (source, params) -> moveDown());
        window.event("bottom", (source, params) -> moveBottom());
        window.event("remove", (source, params) -> removeFromList());
        window.event("scan", (source, params) -> RFToolsStorageMessages.INSTANCE.sendToServer(new PacketGetInventoryInfo(tileEntity.getDimension(), tileEntity.getStorageScannerPos(), true)));

        minecraft.keyboardListener.enableRepeatEvents(true);

        fromServer_foundInventories.clear();
        fromServer_inventory.clear();

        if (tileEntity.isDummy()) {
            fromServer_inventories.clear();
        }

        BlockPos pos = tileEntity.getCraftingGridContainerPos();
        craftingGrid.initGui(modBase, network, minecraft, this, pos, tileEntity.getCraftingGridProvider(), guiLeft, guiTop, xSize, ySize);
        sendServerCommand(RFToolsStorage.MODID, CommandHandler.CMD_REQUEST_GRID_SYNC, TypedMap.builder().put(CommandHandler.PARAM_POS, pos).build());

        if (StorageScannerConfiguration.hilightStarredOnGuiOpen.get()) {
            storageList.setSelected(0);
        }

        init = true;
    }

    private int getStoragePanelWidth() {
        return openViewButton.isPressed() ? 130 : 50;
    }

    private Panel makeItemPanel() {
        itemList = new WidgetList(minecraft, this).setName("items").setPropagateEventsToChildren(true)
                .setInvisibleSelection(true);
        Slider itemListSlider = new Slider(minecraft, this).setDesiredWidth(10).setVertical().setScrollableName("items");
        return new Panel(minecraft, this)
                .setLayout(new HorizontalLayout().setSpacing(1).setHorizontalMargin(1))
                .setLayoutHint(new PositionalLayout.PositionalHint(getStoragePanelWidth() + 6, 4, 256 - getStoragePanelWidth() - 12, 86 + 54))
                .addChild(itemList).addChild(itemListSlider);
    }

    private Panel makeStoragePanel(Panel energyPanel) {
        storageList = new WidgetList(minecraft, this).setName("storage").addSelectionEvent(new DefaultSelectionEvent() {
            @Override
            public void select(Widget<?> parent, int index) {
                getInventoryOnServer();
            }

            @Override
            public void doubleClick(Widget<?> parent, int index) {
                hilightSelectedContainer(index);
            }
        }).setPropagateEventsToChildren(true);

        Slider storageListSlider = new Slider(minecraft, this).setDesiredWidth(10).setVertical().setScrollableName("storage");

        return new Panel(minecraft, this).setLayout(new HorizontalLayout().setSpacing(1).setHorizontalMargin(1))
                .setLayoutHint(new PositionalLayout.PositionalHint(3, 4, getStoragePanelWidth(), 86 + 54))
                .setDesiredHeight(86 + 54)
                .addChild(energyPanel)
                .addChild(storageList).addChild(storageListSlider);
    }

    private void toggleView() {
        storagePanel.setLayoutHint(new PositionalLayout.PositionalHint(3, 4, getStoragePanelWidth(), 86 + 54));
        itemPanel.setLayoutHint(new PositionalLayout.PositionalHint(getStoragePanelWidth() + 6, 4, 256 - getStoragePanelWidth() - 12, 86 + 54));
        // Force layout dirty:
        window.getToplevel().setBounds(window.getToplevel().getBounds());
        listDirty = 0;
        requestListsIfNeeded();
        sendServerCommand(RFToolsStorageMessages.INSTANCE, tileEntity.getDimension(), StorageScannerTileEntity.CMD_SETVIEW,
                TypedMap.builder()
                        .put(PARAM_VIEW, openViewButton.isPressed())
                        .build());
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        boolean r = super.mouseClicked(x, y, button);
        craftingGrid.getWindow().mouseClicked((int)x, (int)y, button);
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
        craftingGrid.getWindow().handleMouseInput(button);  // @todo 1.14 check?
        return super.mouseDragged(x, y, button, scaledX, scaledY);
    }


    @Override
    public boolean mouseReleased(double x, double y, int state) {
        boolean rc = super.mouseReleased(x, y, state);
        craftingGrid.getWindow().mouseMovedOrUp((int)x, (int)y, state);
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
        sendServerCommand(RFToolsStorageMessages.INSTANCE, tileEntity.getDimension(), StorageScannerTileEntity.CMD_UP,
                TypedMap.builder().put(PARAM_INDEX, storageList.getSelected() - 1).build());
        storageList.setSelected(storageList.getSelected() - 1);
        listDirty = 0;
    }

    private void moveTop() {
        sendServerCommand(RFToolsStorageMessages.INSTANCE, tileEntity.getDimension(), StorageScannerTileEntity.CMD_TOP,
                TypedMap.builder().put(PARAM_INDEX, storageList.getSelected() - 1).build());
        storageList.setSelected(1);
        listDirty = 0;
    }

    private void moveDown() {
        sendServerCommand(RFToolsStorageMessages.INSTANCE, tileEntity.getDimension(), StorageScannerTileEntity.CMD_DOWN,
                TypedMap.builder().put(PARAM_INDEX, storageList.getSelected() - 1).build());
        storageList.setSelected(storageList.getSelected() + 1);
        listDirty = 0;
    }

    private void moveBottom() {
        sendServerCommand(RFToolsStorageMessages.INSTANCE, tileEntity.getDimension(), StorageScannerTileEntity.CMD_BOTTOM,
                TypedMap.builder().put(PARAM_INDEX, storageList.getSelected() - 1).build());
        storageList.setSelected(storageList.getChildCount() - 1);
        listDirty = 0;
    }

    private void removeFromList() {
        sendServerCommand(RFToolsStorageMessages.INSTANCE, tileEntity.getDimension(), StorageScannerTileEntity.CMD_REMOVE,
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
            sendServerCommand(RFToolsStorage.MODID, CommandHandler.CMD_SCANNER_SEARCH,
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
            sendServerCommand(RFToolsStorage.MODID, CommandHandler.CMD_REQUEST_SCANNER_CONTENTS,
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

//        Collections.sort(fromServer_inventory, (o1, o2) -> o1.stackSize == o2.stackSize ? 0 : o1.stackSize < o2.stackSize ? -1 : 1);
        Collections.sort(fromServer_inventory, Comparator.comparing(itemStack -> itemStack.getDisplayName().getFormattedText()));

        String filterText = searchField.getText().toLowerCase();
        Predicate<ItemStack> matcher = StorageScannerTileEntity.getMatcher(filterText);

        for (ItemStack item : fromServer_inventory) {
//            String displayName = item.getDisplayName();
            if (filterText.isEmpty() || matcher.test(item)) {
                currentPos = addItemToList(item, itemList, currentPos, numcolumns, spacing);
            }
        }
    }

    private Pair<Panel, Integer> addItemToList(ItemStack item, WidgetList itemList, Pair<Panel, Integer> currentPos, int numcolumns, int spacing) {
        Panel panel = currentPos.getKey();
        if (panel == null || currentPos.getValue() >= numcolumns) {
            panel = new Panel(minecraft, this).setLayout(new HorizontalLayout().setSpacing(spacing).setHorizontalMargin(1))
                    .setDesiredHeight(12).setUserObject(new Integer(-1)).setDesiredHeight(16);
            currentPos = MutablePair.of(panel, 0);
            itemList.addChild(panel);
        }
        BlockRender blockRender = new BlockRender(minecraft, this)
                .setRenderItem(item)
                .setUserObject(1)       // Mark as a special stack in the renderer (for tooltip)
                .setOffsetX(-1)
                .setOffsetY(-1)
                .setHilightOnHover(true);
        blockRender.addSelectionEvent(new BlockRenderEvent() {
            @Override
            public void select(Widget<?> widget) {
                BlockRender br = (BlockRender) widget;
                Object item = br.getRenderItem();
                if (item != null) {
                    boolean shift = McJtyLib.proxy.isShiftKeyDown();
                    requestItem((ItemStack) item, shift ? 1 : -1);
                }
            }

            @Override
            public void doubleClick(Widget<?> widget) {
            }
        });
        panel.addChild(blockRender);
        currentPos.setValue(currentPos.getValue() + 1);
        return currentPos;
    }

    private void requestItem(ItemStack stack, int amount) {
        BlockPos selectedContainerPos = getSelectedContainerPos();
        if (selectedContainerPos == null) {
            return;
        }
        network.sendToServer(new PacketRequestItem(tileEntity.getDimension(), tileEntity.getStorageScannerPos(), selectedContainerPos, stack, amount));
        getInventoryOnServer();
    }

    private void changeRoutable(BlockPos c) {
        sendServerCommand(RFToolsStorageMessages.INSTANCE, tileEntity.getDimension(), StorageScannerTileEntity.CMD_TOGGLEROUTABLE,
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
            panel = new Panel(minecraft, this).setLayout(new HorizontalLayout().setSpacing(8).setHorizontalMargin(5));
            panel.addChild(new ImageLabel(minecraft, this).setImage(guielements, 115, 19).setDesiredWidth(13).setDesiredHeight(13));
        } else {
            HorizontalLayout layout = new HorizontalLayout();
            if (!openViewButton.isPressed()) {
                layout.setHorizontalMargin(2);
            }
            panel = new Panel(minecraft, this).setLayout(layout);
            panel.addChild(new BlockRender(minecraft, this).setRenderItem(c.getBlock()));
        }
        if (openViewButton.isPressed()) {
            AbstractWidget<?> label;
            label = new Label(minecraft, this).setColor(StyleConfig.colorTextInListNormal)
                    .setText(displayName)
                    .setDynamic(true)
                    .setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                    .setDesiredWidth(58);
            if (c == null) {
                label.setTooltips(TextFormatting.GREEN + "All routable inventories")
                        .setDesiredWidth(74);
            } else {
                label.setTooltips(TextFormatting.GREEN + "Block at: " + TextFormatting.WHITE + BlockPosTools.toString(c.getPos()),
                        TextFormatting.GREEN + "Name: " + TextFormatting.WHITE + displayName,
                        "(doubleclick to highlight)");
            }
            panel.addChild(label);
            if (c != null) {
                ImageChoiceLabel choiceLabel = new ImageChoiceLabel(minecraft, this)
                        .addChoiceEvent((parent, newChoice) -> changeRoutable(c.getPos())).setDesiredWidth(13);
                choiceLabel.addChoice("No", "Not routable", guielements, 131, 19);
                choiceLabel.addChoice("Yes", "Routable", guielements, 115, 19);
                choiceLabel.setCurrentChoice(routable ? 1 : 0);
                panel.addChild(choiceLabel);
            }
        }
        storageList.addChild(panel);
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
        visibleRadiusLabel.setText(text);

        int selected = storageList.getSelected();
        removeButton.setEnabled(selected != -1);
        if (selected <= 0 || storageList.getChildCount() <= 2) {
            upButton.setEnabled(false);
            downButton.setEnabled(false);
            topButton.setEnabled(false);
            bottomButton.setEnabled(false);
        } else if (selected == 1) {
            topButton.setEnabled(false);
            upButton.setEnabled(false);
            downButton.setEnabled(true);
            bottomButton.setEnabled(true);
        } else if (selected == storageList.getChildCount() - 1) {
            topButton.setEnabled(true);
            upButton.setEnabled(true);
            downButton.setEnabled(false);
            bottomButton.setEnabled(false);
        } else {
            topButton.setEnabled(true);
            upButton.setEnabled(true);
            downButton.setEnabled(true);
            bottomButton.setEnabled(true);
        }

        if (!tileEntity.isDummy()) {
            tileEntity.getCapability(CapabilityEnergy.ENERGY).ifPresent(e -> {
                energyBar.setMaxValue(((GenericEnergyStorage)e).getCapacity());
                energyBar.setValue(((GenericEnergyStorage)e).getEnergy());
            });
            exportToStarred.setCurrentChoice(tileEntity.isExportToCurrent() ? 0 : 1);
        } else {
            if (System.currentTimeMillis() - lastTime > 300) {
                lastTime = System.currentTimeMillis();
                tileEntity.requestDataFromServer(RFToolsStorageMessages.INSTANCE, StorageScannerTileEntity.CMD_SCANNER_INFO, TypedMap.EMPTY);
            }
            energyBar.setValue(rfReceived);
            exportToStarred.setCurrentChoice(exportToCurrentReceived ? 0 : 1);
        }

        drawWindow();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i1, int i2) {
        if (!init) {
            return;
        }
        double mouseX = minecraft.mouseHelper.getMouseX();
        double mouseY = minecraft.mouseHelper.getMouseY();
        int x = (int) (mouseX * width / minecraft.mainWindow.getWidth());
        int y = (int) (mouseY * height / minecraft.mainWindow.getHeight() - 1);

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
