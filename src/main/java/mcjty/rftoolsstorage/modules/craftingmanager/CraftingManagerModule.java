package mcjty.rftoolsstorage.modules.craftingmanager;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerBlock;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerContainer;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerTileEntity;
import mcjty.rftoolsstorage.modules.craftingmanager.client.CraftingManagerRenderer;
import mcjty.rftoolsstorage.modules.craftingmanager.client.GuiCraftingManager;
import mcjty.rftoolsstorage.modules.craftingmanager.system.CraftingDeviceRegistry;
import mcjty.rftoolsstorage.setup.Registration;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static mcjty.rftoolsstorage.setup.Registration.*;

public class CraftingManagerModule implements IModule {

    public static final RegistryObject<Block> CRAFTING_MANAGER = BLOCKS.register("crafting_manager", CraftingManagerBlock::new);
    public static final RegistryObject<Item> CRAFTING_MANAGER_ITEM = ITEMS.register("crafting_manager", () -> new BlockItem(CRAFTING_MANAGER.get(), Registration.createStandardProperties()));
    public static final RegistryObject<TileEntityType<CraftingManagerTileEntity>> TYPE_CRAFTING_MANAGER = TILES.register("crafting_manager", () -> TileEntityType.Builder.create(CraftingManagerTileEntity::new, CRAFTING_MANAGER.get()).build(null));
    public static final RegistryObject<ContainerType<CraftingManagerContainer>> CONTAINER_CRAFTING_MANAGER = CONTAINERS.register("crafting_manager", GenericContainer::createContainerType);

    public static CraftingDeviceRegistry CRAFTING_DEVICE_REGISTRY;

    public CraftingManagerModule() {
        CRAFTING_DEVICE_REGISTRY = new CraftingDeviceRegistry();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(CraftingManagerModule::modelInit);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {
        CRAFTING_DEVICE_REGISTRY.init();
    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        DeferredWorkQueue.runLater(() -> {
            GenericGuiContainer.register(CONTAINER_CRAFTING_MANAGER.get(), GuiCraftingManager::new);
        });

        RenderTypeLookup.setRenderLayer(CRAFTING_MANAGER.get(), RenderType.getCutout());
    }

    @Override
    public void initConfig() {

    }

    public static void modelInit(ModelRegistryEvent event) {
        CraftingManagerRenderer.register();
    }
}