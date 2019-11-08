package mcjty.rftoolsstorage.modules.modularstorage;

import mcjty.lib.blocks.BaseBlockItem;
import mcjty.lib.container.GenericContainer;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageBlock;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageTileEntity;
import mcjty.rftoolsstorage.modules.modularstorage.items.StorageModuleItem;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ObjectHolder;

public class ModularStorageSetup {

    @ObjectHolder(RFToolsStorage.MODID + ":" + ModularStorageBlock.REGNAME)
    public static ModularStorageBlock MODULAR_STORAGE;
    @ObjectHolder(RFToolsStorage.MODID + ":" + ModularStorageBlock.REGNAME)
    public static TileEntityType<?> TYPE_MODULAR_STORAGE;
    @ObjectHolder(RFToolsStorage.MODID + ":" + ModularStorageBlock.REGNAME)
    public static ContainerType<ModularStorageContainer> CONTAINER_MODULAR_STORAGE;

    @ObjectHolder(RFToolsStorage.MODID + ":storage_module0")
    public static StorageModuleItem STORAGE_MODULE0;
    @ObjectHolder(RFToolsStorage.MODID + ":storage_module1")
    public static StorageModuleItem STORAGE_MODULE1;
    @ObjectHolder(RFToolsStorage.MODID + ":storage_module2")
    public static StorageModuleItem STORAGE_MODULE2;
    @ObjectHolder(RFToolsStorage.MODID + ":storage_module3")
    public static StorageModuleItem STORAGE_MODULE3;
    @ObjectHolder(RFToolsStorage.MODID + ":storage_module6")
    public static StorageModuleItem STORAGE_MODULE_REMOTE;

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new ModularStorageBlock());
    }

    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new StorageModuleItem(StorageModuleItem.STORAGE_TIER1));
        event.getRegistry().register(new StorageModuleItem(StorageModuleItem.STORAGE_TIER2));
        event.getRegistry().register(new StorageModuleItem(StorageModuleItem.STORAGE_TIER3));
        event.getRegistry().register(new StorageModuleItem(StorageModuleItem.STORAGE_TIER4));
        event.getRegistry().register(new StorageModuleItem(StorageModuleItem.STORAGE_REMOTE));
        Item.Properties properties = new Item.Properties().group(RFToolsStorage.setup.getTab());
        event.getRegistry().register(new BaseBlockItem(MODULAR_STORAGE, properties));
    }

    public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().register(TileEntityType.Builder.create(ModularStorageTileEntity::new, MODULAR_STORAGE).build(null).setRegistryName(ModularStorageBlock.REGNAME));
    }

    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().register(GenericContainer.createContainerType(ModularStorageBlock.REGNAME));
    }
}
