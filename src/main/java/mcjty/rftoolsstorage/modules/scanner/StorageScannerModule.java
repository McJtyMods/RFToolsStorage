package mcjty.rftoolsstorage.modules.scanner;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.lib.setup.DeferredBlock;
import mcjty.lib.setup.DeferredItem;
import mcjty.rftoolsbase.modules.tablet.items.TabletItem;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.rftoolsstorage.modules.scanner.blocks.RemoteStorageScannerContainer;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerBlock;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerContainer;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import mcjty.rftoolsstorage.modules.scanner.client.ClientCommandHandler;
import mcjty.rftoolsstorage.modules.scanner.client.GuiStorageScanner;
import mcjty.rftoolsstorage.modules.scanner.items.DumpModuleItem;
import mcjty.rftoolsstorage.modules.scanner.items.StorageControlModuleItem;
import mcjty.rftoolsstorage.setup.Config;
import mcjty.rftoolsstorage.setup.Registration;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.function.Supplier;

import static mcjty.lib.datagen.DataGen.has;
import static mcjty.rftoolsstorage.RFToolsStorage.tab;
import static mcjty.rftoolsstorage.setup.Registration.*;

public class StorageScannerModule implements IModule {

    public static final DeferredBlock<StorageScannerBlock> STORAGE_SCANNER = BLOCKS.register("storage_scanner", StorageScannerBlock::new);
    public static final DeferredItem<Item> STORAGE_SCANNER_ITEM = ITEMS.register("storage_scanner", tab(() -> new BlockItem(STORAGE_SCANNER.get(), Registration.createStandardProperties())));
    public static final Supplier<BlockEntityType<?>> TYPE_STORAGE_SCANNER = TILES.register("storage_scanner", () -> BlockEntityType.Builder.of(StorageScannerTileEntity::new, STORAGE_SCANNER.get()).build(null));
    public static final Supplier<MenuType<StorageScannerContainer>> CONTAINER_STORAGE_SCANNER = CONTAINERS.register("storage_scanner", GenericContainer::createContainerType);
    public static final Supplier<MenuType<RemoteStorageScannerContainer>> CONTAINER_STORAGE_SCANNER_REMOTE = CONTAINERS.register("storage_scanner_remote",
            () -> GenericContainer.createRemoteContainerType(StorageScannerTileEntity::new, StorageScannerContainer::createRemote, StorageScannerContainer.SLOTS));

    public static final DeferredItem<Item> STORAGECONTROL_MODULE = ITEMS.register("storage_control_module", tab(StorageControlModuleItem::new));
    public static final DeferredItem<Item> DUMP_MODULE = ITEMS.register("dump_module", tab(DumpModuleItem::new));

    public static final DeferredItem<TabletItem> TABLET_SCANNER = ITEMS.register("tablet_scanner", tab(TabletItem::new));

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

    @Override
    public void initDatagen(DataGen dataGen) {
        dataGen.add(
                Dob.blockBuilder(STORAGE_SCANNER)
                        .ironPickaxeTags()
                        .standardLoot(TYPE_STORAGE_SCANNER)
                        .shaped(builder -> builder
                                        .define('g', Items.GOLD_INGOT)
                                        .define('F', VariousModule.MACHINE_FRAME.get())
                                        .unlockedBy("frame", has(VariousModule.MACHINE_FRAME.get())),
                                "ToT", "gFg", "ToT"),
                Dob.itemBuilder(STORAGECONTROL_MODULE)
                        .shaped(builder -> builder
                                        .define('X', Items.CRAFTING_TABLE)
                                        .unlockedBy("ingot", has(Items.IRON_INGOT)),
                                " X ", "rir", " X "),
                Dob.itemBuilder(DUMP_MODULE)
                        .shaped(builder -> builder
                                        .define('X', ItemTags.WOODEN_BUTTONS)
                                        .unlockedBy("ingot", has(Items.IRON_INGOT)),
                                " X ", "rir", " X "),
                Dob.itemBuilder(TABLET_SCANNER)
        );
    }
}