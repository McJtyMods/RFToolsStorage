package mcjty.rftoolsstorage.modules.modularstorage;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageBlock;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageTileEntity;
import mcjty.rftoolsstorage.modules.modularstorage.client.GuiModularStorage;
import mcjty.rftoolsstorage.modules.modularstorage.items.StorageModuleItem;
import mcjty.rftoolsstorage.setup.Config;
import mcjty.rftoolsstorage.setup.Registration;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import static mcjty.rftoolsstorage.setup.Registration.*;

public class ModularStorageModule implements IModule {

    public static final RegistryObject<BaseBlock> MODULAR_STORAGE = BLOCKS.register("modular_storage", ModularStorageBlock::new);
    public static final RegistryObject<Item> MODULAR_STORAGE_ITEM = ITEMS.register("modular_storage", () -> new BlockItem(MODULAR_STORAGE.get(), Registration.createStandardProperties()));
    public static final RegistryObject<BlockEntityType<?>> TYPE_MODULAR_STORAGE = TILES.register("modular_storage", () -> BlockEntityType.Builder.of(ModularStorageTileEntity::new, MODULAR_STORAGE.get()).build(null));
    public static final RegistryObject<MenuType<ModularStorageContainer>> CONTAINER_MODULAR_STORAGE = CONTAINERS.register("modular_storage", GenericContainer::createContainerType);

    public static final RegistryObject<StorageModuleItem> STORAGE_MODULE0 = ITEMS.register("storage_module0", () -> new StorageModuleItem(StorageModuleItem.STORAGE_TIER1));
    public static final RegistryObject<StorageModuleItem> STORAGE_MODULE1 = ITEMS.register("storage_module1", () -> new StorageModuleItem(StorageModuleItem.STORAGE_TIER2));
    public static final RegistryObject<StorageModuleItem> STORAGE_MODULE2 = ITEMS.register("storage_module2", () -> new StorageModuleItem(StorageModuleItem.STORAGE_TIER3));
    public static final RegistryObject<StorageModuleItem> STORAGE_MODULE3 = ITEMS.register("storage_module3", () -> new StorageModuleItem(StorageModuleItem.STORAGE_TIER4));
    public static final RegistryObject<StorageModuleItem> STORAGE_MODULE6 = ITEMS.register("storage_module6", () -> new StorageModuleItem(StorageModuleItem.STORAGE_REMOTE));

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GuiModularStorage.register();
        });
    }

    @Override
    public void initConfig() {
        ModularStorageConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }
}
