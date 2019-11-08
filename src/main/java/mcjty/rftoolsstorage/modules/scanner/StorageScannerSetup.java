package mcjty.rftoolsstorage.modules.scanner;

import mcjty.lib.blocks.BaseBlockItem;
import mcjty.lib.container.GenericContainer;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerBlock;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerContainer;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ObjectHolder;

public class StorageScannerSetup {

    @ObjectHolder(RFToolsStorage.MODID + ":storage_scanner")
    public static StorageScannerBlock STORAGE_SCANNER;

    @ObjectHolder(RFToolsStorage.MODID + ":storage_scanner")
    public static ContainerType<StorageScannerContainer> CONTAINER_STORAGE_SCANNER;

    @ObjectHolder(RFToolsStorage.MODID + ":storage_scanner")
    public static TileEntityType<?> TYPE_STORAGE_SCANNER;

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new StorageScannerBlock());
    }

    public static void registerItems(RegistryEvent.Register<Item> event) {
        Item.Properties properties = new Item.Properties().group(RFToolsStorage.setup.getTab());
        event.getRegistry().register(new BaseBlockItem(STORAGE_SCANNER, properties));
    }

    public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().register(TileEntityType.Builder.create(StorageScannerTileEntity::new, STORAGE_SCANNER).build(null).setRegistryName("storage_scanner"));
    }

    public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().register(GenericContainer.createContainerType("storage_scanner"));

    }
}
