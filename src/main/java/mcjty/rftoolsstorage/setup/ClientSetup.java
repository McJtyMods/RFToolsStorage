package mcjty.rftoolsstorage.setup;


import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerSetup;
import mcjty.rftoolsstorage.modules.craftingmanager.client.CraftingManagerRenderer;
import mcjty.rftoolsstorage.modules.craftingmanager.client.GuiCraftingManager;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageSetup;
import mcjty.rftoolsstorage.modules.modularstorage.client.GuiModularStorage;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerSetup;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerContainer;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import mcjty.rftoolsstorage.modules.scanner.client.GuiStorageScanner;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {

    public static void init(FMLClientSetupEvent e) {
        DeferredWorkQueue.runLater(() -> {
            GenericGuiContainer.register(ModularStorageSetup.CONTAINER_MODULAR_STORAGE.get(), GuiModularStorage::new);
            GenericGuiContainer.register(CraftingManagerSetup.CONTAINER_CRAFTING_MANAGER.get(), GuiCraftingManager::new);

            GenericGuiContainer.register(StorageScannerSetup.CONTAINER_STORAGE_SCANNER.get(), GuiStorageScanner::new);
            ScreenManager.IScreenFactory<StorageScannerContainer, GuiStorageScanner> factory = (container, inventory, title) -> {
                TileEntity te = container.getTe();
                return Tools.safeMap(te, (StorageScannerTileEntity tile) -> new GuiStorageScanner(tile, container, inventory), "Invalid tile entity!");
            };
            ScreenManager.registerFactory(StorageScannerSetup.CONTAINER_STORAGE_SCANNER_REMOTE.get(), factory);

            ClientCommandHandler.registerCommands();
        });

        RenderTypeLookup.setRenderLayer(CraftingManagerSetup.CRAFTING_MANAGER.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(ModularStorageSetup.MODULAR_STORAGE.get(), RenderType.getCutout());
    }

    public static void modelInit(ModelRegistryEvent event) {
        CraftingManagerRenderer.register();
    }
}
