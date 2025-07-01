package mcjty.rftoolsstorage.modules.craftingmanager.client;

import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftoolsbase.RFToolsBase;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerModule;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerContainer;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerTileEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import javax.annotation.Nonnull;

import static mcjty.lib.gui.widgets.Widgets.positional;


public class GuiCraftingManager extends GenericGuiContainer<CraftingManagerTileEntity, CraftingManagerContainer> {
    public static final int WIDTH = 256;
    public static final int HEIGHT = 208;

    private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath(RFToolsStorage.MODID, "textures/gui/craftingmanager.png");
    private static final ResourceLocation guiElements = ResourceLocation.fromNamespaceAndPath(RFToolsBase.MODID, "textures/gui/guielements.png");

    public GuiCraftingManager(CraftingManagerContainer container, Inventory inventory, Component title) {
        super(container, inventory, title, ManualEntry.EMPTY);

        imageWidth = WIDTH;
        imageHeight = HEIGHT;
    }

    public static void register(RegisterMenuScreensEvent event) {
        event.register(CraftingManagerModule.CONTAINER_CRAFTING_MANAGER.get(), GuiCraftingManager::new);
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
    protected void renderBg(@Nonnull GuiGraphics graphics, float partialTicks, int x, int y) {
        drawWindow(graphics, partialTicks, x, y);
    }
}
