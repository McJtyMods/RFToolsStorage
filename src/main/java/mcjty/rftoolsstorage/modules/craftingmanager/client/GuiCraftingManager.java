package mcjty.rftoolsstorage.modules.craftingmanager.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftoolsbase.RFToolsBase;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerModule;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerContainer;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerTileEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

import static mcjty.lib.gui.widgets.Widgets.positional;


public class GuiCraftingManager extends GenericGuiContainer<CraftingManagerTileEntity, CraftingManagerContainer> {
    public static final int WIDTH = 256;
    public static final int HEIGHT = 208;

    private static final ResourceLocation background = new ResourceLocation(RFToolsStorage.MODID, "textures/gui/craftingmanager.png");
    private static final ResourceLocation guiElements = new ResourceLocation(RFToolsBase.MODID, "textures/gui/guielements.png");

    public GuiCraftingManager(CraftingManagerTileEntity tileEntity, CraftingManagerContainer container, PlayerInventory inventory) {
        super(tileEntity, container, inventory, ManualEntry.EMPTY);

        imageWidth = WIDTH;
        imageHeight = HEIGHT;
    }

    public static void register() {
        register(CraftingManagerModule.CONTAINER_CRAFTING_MANAGER.get(), GuiCraftingManager::new);
    }

    @Override
    public void init() {
        super.init();

        Panel toplevel = positional();
        toplevel.background(background);
        toplevel.bounds(leftPos, topPos, imageWidth, imageHeight);

        window = new Window(this, toplevel);
    }


    @Override
    protected void renderBg(@Nonnull MatrixStack matrixStack, float v, int i, int i2) {
        drawWindow(matrixStack);
    }
}
