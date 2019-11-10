package mcjty.rftoolsstorage.modules.craftingmanager.client;

import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftoolsbase.RFToolsBase;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerContainer;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerTileEntity;
import mcjty.rftoolsstorage.network.RFToolsStorageMessages;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;


public class GuiCraftingManager extends GenericGuiContainer<CraftingManagerTileEntity, CraftingManagerContainer> {
    public static final int WIDTH = 256;

    private static final ResourceLocation iconLocationTop = new ResourceLocation(RFToolsStorage.MODID, "textures/gui/craftingmanager.png");
    private static final ResourceLocation guiElements = new ResourceLocation(RFToolsBase.MODID, "textures/gui/guielements.png");

    public GuiCraftingManager(CraftingManagerTileEntity tileEntity, CraftingManagerContainer container, PlayerInventory inventory) {
        super(RFToolsStorage.instance, RFToolsStorageMessages.INSTANCE, tileEntity, container, inventory, 0, "storage");

        xSize = WIDTH;
    }

    @Override
    public void init() {
        super.init();
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
    }
}
