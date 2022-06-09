package mcjty.rftoolsstorage.modules.scanner.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.lib.base.StyleConfig;
import mcjty.lib.client.GuiTools;
import mcjty.lib.container.GhostOutputSlot;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.WindowManager;
import mcjty.lib.gui.events.BlockRenderEvent;
import mcjty.lib.gui.events.DefaultSelectionEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.network.PacketRequestDataFromServer;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.*;
import mcjty.rftoolsbase.RFToolsBase;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.craftinggrid.GuiCraftingGrid;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerConfiguration;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerModule;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerContainer;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import mcjty.rftoolsstorage.modules.scanner.network.PacketGetInventoryInfo;
import mcjty.rftoolsstorage.modules.scanner.network.PacketRequestItem;
import mcjty.rftoolsstorage.modules.scanner.network.PacketReturnInventoryInfo;
import mcjty.rftoolsstorage.modules.scanner.tools.SortingMode;
import mcjty.rftoolsstorage.setup.CommandHandler;
import mcjty.rftoolsstorage.setup.RFToolsStorageMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;

import static mcjty.lib.gui.widgets.Widgets.*;
import static mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity.*;


public class GuiStorageScanner extends GenericGuiContainer<StorageScannerTileEntity, StorageScannerContainer> {
    private static final int STORAGE_MONITOR_WIDTH = 256;
    private static final int STORAGE_MONITOR_HEIGHT = 244;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFToolsStorage.MODID, "textures/gui/storagescanner.png");
    private static final ResourceLocation guielements = new ResourceLocation(RFToolsBase.MODID, "textures/gui/guielements.png");
    public static final int LIST_HEIGHT = 86 + 66;

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

    private final GuiCraftingGrid craftingGrid;

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

    public GuiStorageScanner(StorageScannerTileEntity te, StorageScannerContainer container, Inventory playerInventory) {
        super(te, container, playerInventory, StorageScannerModule.STORAGE_SCANNER.get().getManualEntry());

        craftingGrid = new GuiCraftingGrid();

        imageWidth = STORAGE_MONITOR_WIDTH;
        imageHeight = STORAGE_MONITOR_HEIGHT;
    }

    @Override
    protected void registerWindows(WindowManager mgr) {
        super.registerWindows(mgr);
        mgr.addWindow(craftingGrid.getWindow());
    }

    public static void register() {
        register(StorageScannerModule.CONTAINER_STORAGE_SCANNER.get(), GuiStorageScanner::new);
        MenuScreens.ScreenConstructor<StorageScannerContainer, GuiStorageScanner> factory = (container, inventory, title) -> {
            BlockEntity te = container.getTe();
            return Tools.safeMap(te, (StorageScannerTileEntity tile) -> new GuiStorageScanner(tile, container, inventory), "Invalid tile entity!");
        };
        MenuScreens.register(StorageScannerModule.CONTAINER_STORAGE_SCANNER_REMOTE.get(), factory);
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
        energyPanel.children(openViewButton, energyBar, topButton, upButton, downButton, bottomButton, label(" "), label(" "), removeButton);

        exportToStarred = imageChoice(12, 223, 13, 13).name("export");
        exportToStarred.choice("No", "Export to current container", guielements, 131, 19);
        exportToStarred.choice("Yes", "Export to first routable container", guielements, 115, 19);

        storagePanel = makeStoragePanel(energyPanel);
        itemPanel = makeItemPanel();

        radiusLabel = new ScrollableLabel()
                .hint(1, 1, 1, 1)
                .name("radius")
                .visible(false)
                .realMinimum(RFToolsStorage.setup.xnet ? 0 : 1)
                .realMaximum(20);
        visibleRadiusLabel = label(55, 4, 30, 13, "")
                .desiredWidth(30)
                .horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);

        sortChoice = new ChoiceLabel().hint(3, 20, 74, 12).name("sortMode").desiredWidth(60);
        for (SortingMode mode : SortingMode.values()) {
            sortChoice.choices(mode.getDescription()).choiceTooltip(mode.getName(), mode.getTooltip());
        }
        sortChoice.choice(tileEntity.getSortMode().getName());

        searchField = textfield(3, 35, 73, 14)
                .tooltips("Filter the list of items")
                .event((newText) -> {
                    storageList.clearHilightedRows();
                    fromServer_foundInventories.clear();
                    startSearch(newText);
                });

        Slider radiusSlider = new Slider()
                .channel("scan")
                .hint(3, 4, 50, 13)
                .horizontal()
                .tooltips("The radius that the scanner", "will use to find storages")
                .minimumKnobSize(14)
                .scrollableName("radius");
        Panel scanPanel = positional()
                .hint(3, 161, 80, 55)
                .filledRectThickness(-1)
                .filledBackground(StyleConfig.colorListBackground)
                .children(visibleRadiusLabel, radiusLabel, searchField, sortChoice);
        if (!(RFToolsStorage.setup.xnet && StorageScannerConfiguration.xnetRequired.get())) {
            scanPanel.children(radiusSlider);
        }

//        if (tileEntity.isDummy()) {
//            radiusSlider.visible(false);
//        }

        Panel toplevel = positional().background(iconLocation)
                .children(storagePanel, itemPanel, scanPanel, exportToStarred);
        toplevel.bounds(leftPos, topPos, imageWidth, imageHeight);

        window = new Window(this, toplevel);

        window.bind(RFToolsStorageMessages.INSTANCE, "export", tileEntity, "export");
        window.bind(RFToolsStorageMessages.INSTANCE, "radius", tileEntity, StorageScannerTileEntity.VALUE_RADIUS.key().name());
        window.bind(RFToolsStorageMessages.INSTANCE, "sortMode", tileEntity, "sortMode");
        window.event("up", (source, params) -> moveUp());
        window.event("top", (source, params) -> moveTop());
        window.event("down", (source, params) -> moveDown());
        window.event("bottom", (source, params) -> moveBottom());
        window.event("remove", (source, params) -> removeFromList());
        window.event("scan", (source, params) -> RFToolsStorageMessages.INSTANCE.sendToServer(
                new PacketGetInventoryInfo(tileEntity.getDimension(), tileEntity.getStorageScannerPos(), true)));

        minecraft.keyboardHandler.setSendRepeatsToGui(true);

        fromServer_foundInventories.clear();
        fromServer_inventory.clear();
        fromServer_craftable.clear();

        if (tileEntity.isDummy()) {
            fromServer_inventories.clear();
        }

        BlockPos pos = tileEntity.getCraftingGridContainerPos();
        craftingGrid.initGui(RFToolsStorageMessages.INSTANCE, minecraft, this, pos, tileEntity.getDimension(), tileEntity.getCraftingGridProvider(), leftPos, topPos, imageWidth, imageHeight);
        sendServerCommand(RFToolsStorageMessages.INSTANCE, RFToolsStorage.MODID, CommandHandler.CMD_REQUEST_GRID_SYNC, TypedMap.builder()
                .put(CommandHandler.PARAM_POS, pos)
                .put(CommandHandler.PARAM_DIMENSION, tileEntity.getDimension())
                .build());

        if (StorageScannerConfiguration.hilightStarredOnGuiOpen.get()) {
            storageList.selected(0);
        }

        init = true;
    }

    private int getStoragePanelWidth() {
        return openViewButton.isPressed() ? 120 : 45;
    }

    private Panel makeItemPanel() {
        itemList = new WidgetList().name("items").propagateEventsToChildren(true)
                .invisibleSelection(true);
        Slider itemListSlider = new Slider().desiredWidth(9).vertical().scrollableName("items");
        return horizontal(1, 0)
                .hint(getStoragePanelWidth() + 2, 4, 256 - getStoragePanelWidth() - 7, LIST_HEIGHT)
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

        Slider storageListSlider = new Slider().desiredWidth(9).vertical().scrollableName("storage");

        return horizontal(1, 0)
                .hint(3, 4, getStoragePanelWidth(), LIST_HEIGHT)
                .desiredHeight(LIST_HEIGHT)
                .children(energyPanel, storageList, storageListSlider);
    }

    private void toggleView() {
        storagePanel.hint(3, 4, getStoragePanelWidth(), LIST_HEIGHT);
        itemPanel.hint(getStoragePanelWidth() + 2, 4, 256 - getStoragePanelWidth() - 7, LIST_HEIGHT);
        window.getToplevel().markLayoutDirty();
        listDirty = 0;
        requestListsIfNeeded();
        sendServerCommandTyped(RFToolsStorageMessages.INSTANCE, StorageScannerTileEntity.CMD_SETVIEW,
                TypedMap.builder()
                        .put(PARAM_VIEW, openViewButton.isPressed())
                        .build());
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        boolean r = super.mouseClicked(x, y, button);
//        craftingGrid.getWindow().mouseClicked(x, y, button);
        if (button == 1) {
            Slot slot = findSlot(x, y);
            if (slot instanceof GhostOutputSlot) {
                window.sendServerCommand(RFToolsStorageMessages.INSTANCE, CMD_CLEARGRID, TypedMap.EMPTY);
            }
        }
        return r;
    }

    private void moveUp() {
        sendServerCommandTyped(RFToolsStorageMessages.INSTANCE, StorageScannerTileEntity.CMD_UP,
                TypedMap.builder().put(PARAM_INDEX, storageList.getSelected() - 1).build());
        storageList.selected(storageList.getSelected() - 1);
        listDirty = 0;
    }

    private void moveTop() {
        sendServerCommandTyped(RFToolsStorageMessages.INSTANCE, StorageScannerTileEntity.CMD_TOP,
                TypedMap.builder().put(PARAM_INDEX, storageList.getSelected() - 1).build());
        storageList.selected(1);
        listDirty = 0;
    }

    private void moveDown() {
        sendServerCommandTyped(RFToolsStorageMessages.INSTANCE, StorageScannerTileEntity.CMD_DOWN,
                TypedMap.builder().put(PARAM_INDEX, storageList.getSelected() - 1).build());
        storageList.selected(storageList.getSelected() + 1);
        listDirty = 0;
    }

    private void moveBottom() {
        sendServerCommandTyped(RFToolsStorageMessages.INSTANCE, StorageScannerTileEntity.CMD_BOTTOM,
                TypedMap.builder().put(PARAM_INDEX, storageList.getSelected() - 1).build());
        storageList.selected(storageList.getChildCount() - 1);
        listDirty = 0;
    }

    private void removeFromList() {
        sendServerCommandTyped(RFToolsStorageMessages.INSTANCE, StorageScannerTileEntity.CMD_REMOVE,
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
            minecraft.player.closeContainer();
        }
    }

    private void startSearch(String text) {
        if (!text.isEmpty()) {
            sendServerCommand(RFToolsStorageMessages.INSTANCE, RFToolsStorage.MODID, CommandHandler.CMD_SCANNER_SEARCH,
                    TypedMap.builder()
                            .put(CommandHandler.PARAM_SCANNER_DIM, tileEntity.getDimension().location().toString())
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
                            .put(CommandHandler.PARAM_SCANNER_DIM, tileEntity.getDimension().location().toString())
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
                    return info.pos();
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

    @Nonnull
    private static ResourceLocation findLargestTag(ItemStack stack) {
        Collection<TagKey<Item>> tags = TagTools.getTags(stack.getItem());
        if (tags.isEmpty()) {
            return stack.getItem().getRegistryName();
        }
        if (tags.size() == 1) {
            return tags.iterator().next().location();
        }
        int s = -1;
        TagKey<Item> largestTag = null;
        for (TagKey<Item> tag : tags) {
            int size = 0;
            for (Holder<Item> holder : TagTools.getItemsForTag(tag)) {
                size++;
            }
            if (size > s) {
                s = size;
                largestTag = tag;
                break;
            }
        }
        return largestTag.location();
    }

    private static int compareByTag(ItemStack s1, ItemStack s2) {
        ResourceLocation largest1 = findLargestTag(s1);
        ResourceLocation largest2 = findLargestTag(s2);
        int rc = largest1.compareTo(largest2);
        if (rc == 0) {
            return s1.getHoverName().getString() /* was getFormattedText() */.compareTo(s2.getHoverName().getString());
        }
        return rc;
    }

    private static int compareByMod(ItemStack s1, ItemStack s2) {
        int rc = s1.getItem().getRegistryName().getNamespace().compareTo(s2.getItem().getRegistryName().getNamespace());
        if (rc == 0) {
            return s1.getHoverName().getString() /* was getFormattedText() */.compareTo(s2.getHoverName().getString());
        }
        return rc;
    }

    private void updateContentsList() {
        itemList.removeChildren();

        Pair<Panel, Integer> currentPos = MutablePair.of(null, 0);
        int numcolumns = openViewButton.isPressed() ? 6 : 10;
        int spacing = 3;

        SortingMode mode = SortingMode.byDescription(sortChoice.getCurrentChoice());
        if (mode != null) {
            switch (mode) {
                case AMOUNT_ASCENDING -> {
                    fromServer_inventory.sort(Comparator.comparing(ItemStack::getCount));
                    fromServer_craftable.sort(Comparator.comparing(ItemStack::getCount));
                }
                case AMOUNT_DESCENDING -> {
                    fromServer_inventory.sort(Comparator.comparing(ItemStack::getCount).reversed());
                    fromServer_craftable.sort(Comparator.comparing(ItemStack::getCount).reversed());
                }
                case MOD -> {
                    fromServer_inventory.sort(GuiStorageScanner::compareByMod);
                    fromServer_craftable.sort(GuiStorageScanner::compareByMod);
                }
                case TAG -> {
                    fromServer_inventory.sort(GuiStorageScanner::compareByTag);
                    fromServer_craftable.sort(GuiStorageScanner::compareByTag);
                }
                case NAME -> {
                    fromServer_inventory.sort(Comparator.comparing(itemStack -> itemStack.getHoverName().getString()));
                    fromServer_craftable.sort(Comparator.comparing(itemStack -> itemStack.getHoverName().getString()));
                }
            }
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
                    .desiredHeight(12).desiredHeight(16);
            currentPos = MutablePair.of(panel, 0);
            itemList.children(panel);
        }
        BlockRender blockRender = new BlockRender()
                .renderItem(item)
                .userObject(craftable)       // Mark as a special stack in the renderer (for tooltip)
                .offsetX(0)
                .offsetY(-1)
                .hilightOnHover(true);
        if (craftable) {
            // @todo is this looking nice?
//            blockRender.filledBackground(0xff55eeaa, 0xff5588ee);
            blockRender.filledBackground(0xff113366, 0xff115522);
        }
        blockRender.event(new BlockRenderEvent() {
            @Override
            public void select() {
                Object item = blockRender.getRenderItem();
                if (item != null) {
                    boolean shift = SafeClientTools.isSneaking();
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
        sendServerCommandTyped(RFToolsStorageMessages.INSTANCE, StorageScannerTileEntity.CMD_TOGGLEROUTABLE,
                TypedMap.builder().put(PARAM_POS, c).build());
        listDirty = 0;
    }

    private void updateStorageList() {
        storageList.removeChildren();
        addStorageLine(null, "All routable", false);
        for (PacketReturnInventoryInfo.InventoryInfo c : fromServer_inventories) {
            String displayName = c.name();
            boolean routable = c.routable();
            addStorageLine(c, displayName, routable);
        }


        storageList.clearHilightedRows();
        int i = 0;
        for (PacketReturnInventoryInfo.InventoryInfo c : fromServer_inventories) {
            if (fromServer_foundInventories.contains(c.pos())) {
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
            panel.children(new BlockRender().renderItem(c.block()));
        }
        if (openViewButton.isPressed()) {
            AbstractWidget<?> label;
            label = label(displayName)
                    .color(StyleConfig.colorTextInListNormal)
                    .dynamic(true)
                    .horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                    .desiredWidth(50);
            if (c == null) {
                label.tooltips(ChatFormatting.GREEN + "All routable inventories")
                        .desiredWidth(74);
            } else {
                label.tooltips(ChatFormatting.GREEN + "Block at: " + ChatFormatting.WHITE + BlockPosTools.toString(c.pos()),
                        ChatFormatting.GREEN + "Name: " + ChatFormatting.WHITE + displayName,
                        "(doubleclick to highlight)");
            }
            panel.children(label);
            if (c != null) {
                ImageChoiceLabel choiceLabel = new ImageChoiceLabel()
                        .event((newChoice) -> changeRoutable(c.pos())).desiredWidth(13);
                choiceLabel.choice("No", "Not routable", guielements, 131, 19);
                choiceLabel.choice("Yes", "Routable", guielements, 115, 19);
                choiceLabel.setCurrentChoice(routable ? 1 : 0);
                panel.children(choiceLabel);
            }
        }
        storageList.children(panel);
    }

    @Override
    protected void renderBg(@Nonnull PoseStack matrixStack, float v, int i, int i2) {
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
            updateEnergyBar(energyBar);
            exportToStarred.setCurrentChoice(tileEntity.isExportToCurrent() ? 0 : 1);
        } else {
            if (System.currentTimeMillis() - lastTime > 300) {
                lastTime = System.currentTimeMillis();
                RFToolsStorageMessages.INSTANCE.sendToServer(new PacketRequestDataFromServer(tileEntity.getDimension(),
                        tileEntity.getBlockPos(), StorageScannerTileEntity.CMD_SCANNER_INFO, TypedMap.EMPTY, tileEntity.isDummy()));
            }
            energyBar.value(tileEntity.rfReceived);
            exportToStarred.setCurrentChoice(tileEntity.exportToCurrentReceived ? 0 : 1);
        }

        drawWindow(matrixStack);
    }

    @Override
    protected void renderLabels(@Nonnull PoseStack matrixStack, int i1, int i2) {
        if (!init) {
            return;
        }
        int x = GuiTools.getRelativeX(this);
        int y = GuiTools.getRelativeY(this);

        List<String> tooltips = craftingGrid.getWindow().getTooltips();
        if (tooltips != null) {
            drawHoveringText(matrixStack, tooltips, window.getTooltipItems(), x - leftPos, y - topPos, minecraft.font);
        }

        super.renderLabels(matrixStack, i1, i2);
    }

    @Override
    protected void drawStackTooltips(PoseStack matrixStack, int mouseX, int mouseY) {
        if (init) {
            super.drawStackTooltips(matrixStack, mouseX, mouseY);
        }
    }

    @Override
    protected List<Component> addCustomLines(List<Component> oldList, BlockRender blockRender, ItemStack stack) {
        if (blockRender.getUserObject() instanceof Boolean craftable) {
            List<Component> newlist = new ArrayList<>();
            if (craftable) {
                newlist.add(ComponentFactory.literal("Craftable").withStyle(ChatFormatting.GOLD));
            }
            newlist.add(ComponentFactory.literal("Click: ").withStyle(ChatFormatting.GREEN)
                    .append(ComponentFactory.literal("full stack").withStyle(ChatFormatting.WHITE)));
            newlist.add(ComponentFactory.literal("Shift + click: ").withStyle(ChatFormatting.GREEN)
                    .append(ComponentFactory.literal("single item").withStyle(ChatFormatting.WHITE)));
            newlist.add(ComponentFactory.literal(""));
            newlist.addAll(oldList);
            return newlist;
        } else {
            return oldList;
        }
    }

    private static long lastTime = 0;

    @Override
    protected void drawWindow(PoseStack matrixStack) {
        if (!init) {
            return;
        }
        super.drawWindow(matrixStack);
        craftingGrid.draw(matrixStack);
    }

}
