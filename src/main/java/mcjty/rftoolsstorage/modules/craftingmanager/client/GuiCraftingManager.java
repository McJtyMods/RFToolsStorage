package mcjty.rftoolsstorage.modules.craftingmanager.client;

import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftoolsbase.RFToolsBase;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerContainer;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerTileEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;

import java.awt.*;


public class GuiCraftingManager extends GenericGuiContainer<CraftingManagerTileEntity, CraftingManagerContainer> {
    public static final int WIDTH = 256;
    public static final int HEIGHT = 208;

    private static final ResourceLocation background = new ResourceLocation(RFToolsStorage.MODID, "textures/gui/craftingmanager.png");
    private static final ResourceLocation guiElements = new ResourceLocation(RFToolsBase.MODID, "textures/gui/guielements.png");

    public GuiCraftingManager(CraftingManagerTileEntity tileEntity, CraftingManagerContainer container, PlayerInventory inventory) {
        super(RFToolsStorage.instance, tileEntity, container, inventory, 0, "crafting");

        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    public void init() {
        super.init();

        mcjty.lib.gui.widgets.Panel toplevel = new Panel(minecraft, this).setLayout(new PositionalLayout());
        toplevel.setBackground(background);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
    }
}
