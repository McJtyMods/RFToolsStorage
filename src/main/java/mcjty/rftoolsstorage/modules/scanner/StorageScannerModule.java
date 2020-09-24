package mcjty.rftoolsstorage.modules.scanner;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbase.modules.tablet.items.TabletItem;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerBlock;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerContainer;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import mcjty.rftoolsstorage.modules.scanner.client.ClientCommandHandler;
import mcjty.rftoolsstorage.modules.scanner.client.GuiStorageScanner;
import mcjty.rftoolsstorage.modules.scanner.items.DumpModuleItem;
import mcjty.rftoolsstorage.modules.scanner.items.StorageControlModuleItem;
import mcjty.rftoolsstorage.setup.Config;
import mcjty.rftoolsstorage.setup.Registration;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static mcjty.rftoolsstorage.setup.Registration.*;

public class StorageScannerModule implements IModule {

    public static final RegistryObject<StorageScannerBlock> STORAGE_SCANNER = BLOCKS.register("storage_scanner", StorageScannerBlock::new);
    public static final RegistryObject<Item> STORAGE_SCANNER_ITEM = ITEMS.register("storage_scanner", () -> new BlockItem(STORAGE_SCANNER.get(), Registration.createStandardProperties()));
    public static final RegistryObject<TileEntityType<?>> TYPE_STORAGE_SCANNER = TILES.register("storage_scanner", () -> TileEntityType.Builder.create(StorageScannerTileEntity::new, STORAGE_SCANNER.get()).build(null));
    public static final RegistryObject<ContainerType<StorageScannerContainer>> CONTAINER_STORAGE_SCANNER = CONTAINERS.register("storage_scanner", GenericContainer::createContainerType);
    public static final RegistryObject<ContainerType<StorageScannerContainer>> CONTAINER_STORAGE_SCANNER_REMOTE = CONTAINERS.register("storage_scanner_remote",
            () -> GenericContainer.createRemoteContainerType(StorageScannerTileEntity::new, StorageScannerContainer::createRemote, StorageScannerContainer.SLOTS));

    public static final RegistryObject<Item> STORAGECONTROL_MODULE = ITEMS.register("storage_control_module", StorageControlModuleItem::new);
    public static final RegistryObject<Item> DUMP_MODULE = ITEMS.register("dump_module", DumpModuleItem::new);

    public static final RegistryObject<TabletItem> TABLET_SCANNER = ITEMS.register("tablet_scanner", TabletItem::new);

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GuiStorageScanner.register();
            ClientCommandHandler.registerCommands();
        });
    }

    @Override
    public void initConfig() {
        StorageScannerConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }
}