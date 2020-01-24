package mcjty.rftoolsstorage.setup;


import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerSetup;
import mcjty.rftoolsstorage.modules.craftingmanager.client.CraftingManagerBakedModel;
import mcjty.rftoolsstorage.modules.craftingmanager.client.CraftingManagerRenderer;
import mcjty.rftoolsstorage.modules.craftingmanager.client.GuiCraftingManager;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageSetup;
import mcjty.rftoolsstorage.modules.modularstorage.client.GuiModularStorage;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerSetup;
import mcjty.rftoolsstorage.modules.scanner.client.GuiStorageScanner;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RFToolsStorage.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistration {

    @SubscribeEvent
    public static void init(FMLClientSetupEvent e) {
        GenericGuiContainer.register(ModularStorageSetup.CONTAINER_MODULAR_STORAGE.get(), GuiModularStorage::new);
        GenericGuiContainer.register(StorageScannerSetup.CONTAINER_STORAGE_SCANNER.get(), GuiStorageScanner::new);
        GenericGuiContainer.register(CraftingManagerSetup.CONTAINER_CRAFTING_MANAGER.get(), GuiCraftingManager::new);

        RenderTypeLookup.setRenderLayer(CraftingManagerSetup.CRAFTING_MANAGER.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModularStorageSetup.MODULAR_STORAGE.get(), RenderType.cutout());
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        CraftingManagerRenderer.register();
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (!event.getMap().getBasePath().equals("textures")) {
            return;
        }
        event.addSprite(new ResourceLocation(RFToolsStorage.MODID, "block/machinecraftingmanager"));
        event.addSprite(new ResourceLocation(RFToolsStorage.MODID, "block/machinecraftingmanager_top"));
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        event.getModelRegistry().put(new ModelResourceLocation(new ResourceLocation(RFToolsStorage.MODID, "crafting_manager"), ""),
                new CraftingManagerBakedModel());
        event.getModelRegistry().put(new ModelResourceLocation(new ResourceLocation(RFToolsStorage.MODID, "crafting_manager"), "inventory"),
                new CraftingManagerBakedModel());
    }

}
