package mcjty.rftoolsstorage.modules.modularstorage;

import mcjty.lib.container.GenericContainer;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageBlock;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageTileEntity;
import mcjty.rftoolsstorage.modules.modularstorage.items.StorageModuleItem;
import mcjty.rftoolsstorage.setup.Registration;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;

import static mcjty.rftoolsstorage.setup.Registration.*;

public class ModularStorageSetup {

    public static void register() {
        // Needed to force class loading
    }

    public static final RegistryObject<Block> MODULAR_STORAGE = BLOCKS.register("modular_storage", ModularStorageBlock::new);
    public static final RegistryObject<Item> MODULAR_STORAGE_ITEM = ITEMS.register("modular_storage", () -> new BlockItem(MODULAR_STORAGE.get(), Registration.createStandardProperties()));
    public static final RegistryObject<TileEntityType<?>> TYPE_MODULAR_STORAGE = TILES.register("modular_storage", () -> TileEntityType.Builder.create(ModularStorageTileEntity::new, MODULAR_STORAGE.get()).build(null));
    public static final RegistryObject<ContainerType<ModularStorageContainer>> CONTAINER_MODULAR_STORAGE = CONTAINERS.register("modular_storage", GenericContainer::createContainerType);

    public static final RegistryObject<StorageModuleItem> STORAGE_MODULE0 = ITEMS.register("storage_module0", () -> new StorageModuleItem(StorageModuleItem.STORAGE_TIER1));
    public static final RegistryObject<StorageModuleItem> STORAGE_MODULE1 = ITEMS.register("storage_module1", () -> new StorageModuleItem(StorageModuleItem.STORAGE_TIER2));
    public static final RegistryObject<StorageModuleItem> STORAGE_MODULE2 = ITEMS.register("storage_module2", () -> new StorageModuleItem(StorageModuleItem.STORAGE_TIER3));
    public static final RegistryObject<StorageModuleItem> STORAGE_MODULE3 = ITEMS.register("storage_module3", () -> new StorageModuleItem(StorageModuleItem.STORAGE_TIER4));
    public static final RegistryObject<StorageModuleItem> STORAGE_MODULE6 = ITEMS.register("storage_module6", () -> new StorageModuleItem(StorageModuleItem.STORAGE_REMOTE));
}
