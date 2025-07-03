package mcjty.rftoolsstorage.modules.scanner;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbase.modules.tablet.items.TabletItem;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerBlock;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerTileEntity;
import mcjty.rftoolsstorage.modules.modularstorage.data.ModularStorageData;
import mcjty.rftoolsstorage.modules.scanner.blocks.RemoteStorageScannerContainer;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerBlock;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerContainer;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import mcjty.rftoolsstorage.modules.scanner.client.ClientCommandHandler;
import mcjty.rftoolsstorage.modules.scanner.client.GuiStorageScanner;
import mcjty.rftoolsstorage.modules.scanner.data.StorageScannerData;
import mcjty.rftoolsstorage.modules.scanner.items.DumpModuleItem;
import mcjty.rftoolsstorage.modules.scanner.items.StorageControlModuleItem;
import mcjty.rftoolsstorage.setup.Config;
import mcjty.rftoolsstorage.setup.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Supplier;

import static mcjty.lib.datagen.DataGen.has;
import static mcjty.rftoolsstorage.RFToolsStorage.tab;
import static mcjty.rftoolsstorage.setup.Registration.*;

public class StorageScannerModule implements IModule {

    public static final RBlock<BaseBlock, BlockItem, StorageScannerTileEntity> STORAGE_SCANNER = RBLOCKS.registerBlock("storage_scanner",
            StorageScannerTileEntity.class,
            StorageScannerBlock::new,
            block -> new BlockItem(block.get(), createStandardProperties()),
            StorageScannerTileEntity::new
    );
    public static final Supplier<MenuType<StorageScannerContainer>> CONTAINER_STORAGE_SCANNER = CONTAINERS.register("storage_scanner", GenericContainer::createContainerType);
    public static final Supplier<MenuType<RemoteStorageScannerContainer>> CONTAINER_STORAGE_SCANNER_REMOTE = CONTAINERS.register("storage_scanner_remote",
            () -> GenericContainer.createRemoteContainerType(StorageScannerTileEntity::new, StorageScannerContainer::createRemote, StorageScannerContainer.SLOTS));

    public static final DeferredItem<Item> STORAGECONTROL_MODULE = ITEMS.register("storage_control_module", tab(StorageControlModuleItem::new));
    public static final DeferredItem<Item> DUMP_MODULE = ITEMS.register("dump_module", tab(DumpModuleItem::new));

    public static final DeferredItem<TabletItem> TABLET_SCANNER = ITEMS.register("tablet_scanner", tab(TabletItem::new));

    public static final Supplier<AttachmentType<StorageScannerData>> STORAGE_SCANNER_DATA = ATTACHMENT_TYPES.register(
            "storage_scanner_data", () -> AttachmentType.builder(() -> StorageScannerData.DEFAULT)
                    .serialize(StorageScannerData.CODEC)
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<StorageScannerData>> ITEM_STORAGE_SCANNER_DATA = COMPONENTS.registerComponentType(
            "storage_scanner_data",
            builder -> builder
                    .persistent(StorageScannerData.CODEC)
                    .networkSynchronized(StorageScannerData.STREAM_CODEC));

    public StorageScannerModule(IEventBus bus) {
        bus.addListener(this::registerMenuScreens);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientCommandHandler.registerCommands();
        });
    }

    public void registerMenuScreens(RegisterMenuScreensEvent event) {
        GuiStorageScanner.register(event);
    }

    @Override
    public void initConfig(IEventBus bus) {
        StorageScannerConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }

    @Override
    public void initDatagen(DataGen dataGen, HolderLookup.Provider provider) {
        dataGen.add(
                Dob.blockBuilder(STORAGE_SCANNER)
                        .ironPickaxeTags()
                        .standardLoot(ITEM_STORAGE_SCANNER_DATA.get())
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