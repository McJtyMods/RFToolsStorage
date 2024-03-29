package mcjty.rftoolsstorage.craftinggrid;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.lib.base.StyleConfig;
import mcjty.lib.client.GuiTools;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.DefaultSelectionEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.setup.CommandHandler;
import mcjty.rftoolsstorage.setup.RFToolsStorageMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import javax.annotation.Nonnull;
import java.util.Optional;

import static mcjty.lib.gui.widgets.Widgets.*;
import static mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer.CONTAINER_GRID;


public class GuiCraftingGrid {

    private static final ResourceLocation iconLocation = new ResourceLocation(RFToolsStorage.MODID, "textures/gui/craftinggrid.png");

    protected Window craftWindow;
    private Button craft1Button;
    private Button craft4Button;
    private Button craft8Button;
    private Button craftSButton;
    private Button storeButton;
    private WidgetList recipeList;

    private Minecraft mc;
    private GenericGuiContainer<?, ?> gui;
    private CraftingGridProvider provider;
    private BlockPos pos;
    private RegistryKey<World> type;

    public static int[] testResultFromServer = null;
    private int lastTestAmount = -2;
    private int lastTestTimer = 0;

    public void initGui(final SimpleChannel network, final Minecraft mc, GenericGuiContainer<?, ?> gui,
                        BlockPos pos, RegistryKey<World> type, CraftingGridProvider provider,
                        int guiLeft, int guiTop, int xSize, int ySize) {
        this.mc = mc;
        this.gui = gui;
        this.provider = provider;
        this.pos = pos;
        this.type = type;

        recipeList = list(5, 5, 56, 102);
        recipeList.event(new DefaultSelectionEvent() {
            @Override
            public void doubleClick(int index) {
                selectRecipe();
            }
        });
        craft1Button = button(29, 183, 14, 10, "1").channel("craft1").tooltips("Craft one");
        craft4Button = button(45, 183, 14, 10, "4").channel("craft4").tooltips("Craft four");
        craft8Button = button(29, 195, 14, 10, "8").channel("craft8").tooltips("Craft eight");
        craftSButton = button(45, 195, 14, 10, "*").channel("craftstack").tooltips("Craft a stack");
        storeButton = button(5, 109, 56, 14, "Store").channel("store").tooltips("Store the current recipe");
        Panel sidePanel = Widgets.positional()
                .children(craft1Button, craft4Button, craft8Button, craftSButton, storeButton, recipeList);
        int sideLeft = guiLeft - CraftingGridInventory.GRID_WIDTH - 2;
        int sideTop = guiTop;
        sidePanel.bounds(sideLeft, sideTop, CraftingGridInventory.GRID_WIDTH, CraftingGridInventory.GRID_HEIGHT);
        sidePanel.background(iconLocation);
        craftWindow = new Window(gui, sidePanel);

        craftWindow.event("craft1", (source, params) -> craft(1));
        craftWindow.event("craft4", (source, params) -> craft(4));
        craftWindow.event("craft8", (source, params) -> craft(8));
        craftWindow.event("craftstack", (source, params) -> craft(-1));
        craftWindow.event("store", (source, params) -> store());
    }

    public Window getWindow() {
        return craftWindow;
    }

    private void craft(int n) {
        RFToolsStorageMessages.sendToServer(CommandHandler.CMD_CRAFT_FROM_GRID,
                TypedMap.builder()
                        .put(CommandHandler.PARAM_COUNT, n)
                        .put(CommandHandler.PARAM_TEST, false)
                        .put(CommandHandler.PARAM_POS, pos)
                        .put(CommandHandler.PARAM_DIMENSION, type));
    }

    private void testCraft(int n) {
        if (lastTestAmount != n || lastTestTimer <= 0) {
            RFToolsStorageMessages.sendToServer(CommandHandler.CMD_CRAFT_FROM_GRID,
                    TypedMap.builder()
                            .put(CommandHandler.PARAM_COUNT, n)
                            .put(CommandHandler.PARAM_TEST, true)
                            .put(CommandHandler.PARAM_POS, pos)
                            .put(CommandHandler.PARAM_DIMENSION, type));
            lastTestAmount = n;
            lastTestTimer = 20;
        }
        lastTestTimer--;
    }

    private void store() {
        int selected = recipeList.getSelected();
        if (selected == -1) {
            return;
        }
        provider.storeRecipe(selected);
        RFToolsStorageMessages.INSTANCE.sendToServer(new PacketGridToServer(pos, type, provider.getCraftingGrid()));
    }

    private void selectRecipe() {
        int selected = recipeList.getSelected();
        if (selected == -1) {
            return;
        }

        provider.getCraftingGrid().selectRecipe(selected);
        RFToolsStorageMessages.INSTANCE.sendToServer(new PacketGridToServer(pos, type, provider.getCraftingGrid()));
    }

    private void populateList() {
        recipeList.removeChildren();
        for (int i = 0; i < 6; i++) {
            CraftingRecipe recipe = provider.getCraftingGrid().getRecipe(i);
            addRecipeLine(recipe.getResult());
        }
    }

    public void draw(MatrixStack matrixStack) {
        int selected = recipeList.getSelected();
        storeButton.enabled(selected != -1);
        populateList();
        testRecipe();

        int x = GuiTools.getRelativeX(gui);
        int y = GuiTools.getRelativeY(gui);
        Widget<?> widget = craftWindow.getToplevel().getWidgetAtPosition(x, y);
        if (widget == craft1Button) {
            testCraft(1);
        } else if (widget == craft4Button) {
            testCraft(4);
        } else if (widget == craft8Button) {
            testCraft(8);
        } else if (widget == craftSButton) {
            testCraft(-1);
        } else {
            testResultFromServer = null;
            lastTestAmount = -2;
            lastTestTimer = 0;
        }

        craftWindow.draw(matrixStack);

        if (testResultFromServer != null) {
            matrixStack.pushPose();
            matrixStack.translate(gui.getGuiLeft(), gui.getGuiTop(), 0.0F);

            if (testResultFromServer[9] > 0) {
                Container container = gui.getMenu();
                if (container instanceof GenericContainer) {
                    Slot slot = ((GenericContainer) container).getSlotByInventoryAndIndex(CONTAINER_GRID, CraftingGridInventory.SLOT_GHOSTOUTPUT);
                    if (slot != null) {
                        GlStateManager._colorMask(true, true, true, false);
                        int xPos = slot.x;
                        int yPos = slot.y;
                        AbstractGui.fill(matrixStack, xPos, yPos, xPos + 16, yPos + 16, 0xffff0000);
                    }
                }
            }
            for (int i = 0; i < 9; i++) {
                if (testResultFromServer[i] > 0) {
                    Container container = gui.getMenu();
                    if (container instanceof GenericContainer) {
                        Slot slot = ((GenericContainer) container).getSlotByInventoryAndIndex(CONTAINER_GRID, CraftingGridInventory.SLOT_GHOSTINPUT + i);
                        if (slot != null) {
                            GlStateManager._colorMask(true, true, true, false);
                            int xPos = slot.x;
                            int yPos = slot.y;
                            AbstractGui.fill(matrixStack, xPos, yPos, xPos + 16, yPos + 16, 0xffff0000);
                        }
                    }
                }
            }
            matrixStack.popPose();
        }
    }

    private void testRecipe() {
        CraftingInventory inv = new CraftingInventory(new Container(null, -1) {
            @Override
            public boolean stillValid(@Nonnull PlayerEntity var1) {
                return false;
            }
        }, 3, 3);

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, provider.getCraftingGrid().getCraftingGridInventory().getStackInSlot(i + 1));
        }

        // Compare current contents to avoid unneeded slot update.
        Optional<ICraftingRecipe> recipe = CraftingRecipe.findRecipe(mc.level, inv);
        ItemStack newResult = recipe.map(r -> r.assemble(inv)).orElse(ItemStack.EMPTY);
        provider.getCraftingGrid().getCraftingGridInventory().setStackInSlot(0, newResult);
    }

    private void addRecipeLine(ItemStack craftingResult) {
        String readableName = Tools.getReadableName(craftingResult);
        int color = StyleConfig.colorTextInListNormal;
        if (craftingResult.isEmpty()) {
            readableName = "<recipe>";
            color = 0xFF505050;
        }
        Panel panel = Widgets.positional().children(
                new BlockRender()
                        .renderItem(craftingResult)
                        .hint(0, 0, 18, 18),
                label(readableName)
                        .color(color)
                        .horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                        .hint(20, 0, 30, 18));

        recipeList.children(panel);
    }

}
