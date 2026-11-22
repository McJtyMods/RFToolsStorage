package mcjty.rftoolsstorage.modules.modularstorage;

import net.minecraft.resources.Identifier;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageBlock;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageTileEntity;
import mcjty.rftoolsstorage.modules.modularstorage.data.ModularStorageData;
import mcjty.rftoolsstorage.modules.modularstorage.data.StorageModuleData;
import mcjty.rftoolsstorage.modules.modularstorage.items.StorageModuleItem;
import mcjty.rftoolsstorage.setup.Config;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import mcjty.lib.setup.DeferredHolder;
import mcjty.lib.setup.DeferredItem;
import java.util.function.Supplier;

import static mcjty.lib.datagen.DataGen.has;
import static mcjty.rftoolsstorage.RFToolsStorage.tab;
import static mcjty.rftoolsstorage.setup.Registration.*;

public class ModularStorageModule implements IModule {

    public static final RBlock<BaseBlock, BlockItem, ModularStorageTileEntity> MODULAR_STORAGE = RBLOCKS.registerBlock("modular_storage",
            ModularStorageTileEntity.class,
            ModularStorageBlock::new,
            (block, properties) -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix()),
            ModularStorageTileEntity::new
    );
    public static final Supplier<MenuType<ModularStorageContainer>> CONTAINER_MODULAR_STORAGE = CONTAINERS.register("modular_storage", GenericContainer::createContainerType);

    public static final DeferredItem<StorageModuleItem> STORAGE_MODULE0 = ITEMS.register("storage_module0", tab(properties -> new StorageModuleItem(properties, StorageModuleItem.STORAGE_TIER1)));
    public static final DeferredItem<StorageModuleItem> STORAGE_MODULE1 = ITEMS.register("storage_module1", tab(properties -> new StorageModuleItem(properties, StorageModuleItem.STORAGE_TIER2)));
    public static final DeferredItem<StorageModuleItem> STORAGE_MODULE2 = ITEMS.register("storage_module2", tab(properties -> new StorageModuleItem(properties, StorageModuleItem.STORAGE_TIER3)));
    public static final DeferredItem<StorageModuleItem> STORAGE_MODULE3 = ITEMS.register("storage_module3", tab(properties -> new StorageModuleItem(properties, StorageModuleItem.STORAGE_TIER4)));
    public static final DeferredItem<StorageModuleItem> STORAGE_MODULE6 = ITEMS.register("storage_module6", properties -> new StorageModuleItem(properties, StorageModuleItem.STORAGE_REMOTE));   // @todo no tab yet

    public static final AttachmentType<ModularStorageData> MODULAR_STORAGE_DATA = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath("rftoolsstorage", "modular_storage_data"),
            builder -> builder.initializer(() -> new ModularStorageData(false)).persistent(ModularStorageData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ModularStorageData>> ITEM_MODULAR_STORAGE_DATA = COMPONENTS.registerComponentType(
            "modular_storage_data",
            builder -> builder
                    .persistent(ModularStorageData.CODEC)
                    .networkSynchronized(ModularStorageData.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<StorageModuleData>> ITEM_STORAGE_MODULE_DATA = COMPONENTS.registerComponentType(
            "storage_module_data",
            builder -> builder
                    .persistent(StorageModuleData.CODEC)
                    .networkSynchronized(StorageModuleData.STREAM_CODEC));

    @Override
    public void init() {
    }

    @Override
    public void initClient() {
    }

    @Override
    public void initConfig() {
        ModularStorageConfiguration.init(Config.SERVER_BUILDER, Config.CLIENT_BUILDER);
    }

    @Override
    public void initDatagen(DataGen dataGen, HolderLookup.Provider provider) {
        dataGen.add(
                Dob.blockBuilder(MODULAR_STORAGE)
                        .ironPickaxeTags()
                        .standardLoot(ModularStorageModule.ITEM_MODULAR_STORAGE_DATA.get())
                        .shaped(builder -> builder
                                        .define('q', Items.QUARTZ)
                                        .define('C', ConventionalItemTags.CHESTS)
                                        .define('F', VariousModule.MACHINE_FRAME.get())
                                        .unlockedBy("frame", has(VariousModule.MACHINE_FRAME.get())),
                                "rCr", "qFq", "rqr"),
                Dob.itemBuilder(STORAGE_MODULE0)
                        .shaped(builder -> builder
                                        .define('q', Items.QUARTZ)
                                        .define('C', ConventionalItemTags.CHESTS)
                                        .define('g', Items.GOLD_NUGGET)
                                        .unlockedBy("redstone", has(Items.REDSTONE)),
                                " C ", "gig", "qrq"),
                Dob.itemBuilder(STORAGE_MODULE1)
                        .shapedComponentPreserve(builder -> builder
                                        .define('q', Items.QUARTZ)
                                        .define('C', ConventionalItemTags.CHESTS)
                                        .define('g', Items.GOLD_INGOT)
                                        .define('X', STORAGE_MODULE0.get())
                                        .unlockedBy("storage", has(STORAGE_MODULE0.get())),
                                " C ", "gXg", "qrq"),
                Dob.itemBuilder(STORAGE_MODULE2)
                        .shapedComponentPreserve(builder -> builder
                                        .define('C', ConventionalItemTags.CHESTS)
                                        .define('g', Items.GOLD_BLOCK)
                                        .define('X', STORAGE_MODULE1.get())
                                        .define('Q', Items.QUARTZ_BLOCK)
                                        .unlockedBy("storage", has(STORAGE_MODULE1.get())),
                                " C ", "gXg", "QRQ"),
                Dob.itemBuilder(STORAGE_MODULE3)
                        .shapedComponentPreserve(builder -> builder
                                        .define('C', ConventionalItemTags.CHESTS)
                                        .define('Q', Items.QUARTZ_BLOCK)
                                        .define('g', Items.DIAMOND_BLOCK)
                                        .define('t', VariousModule.INFUSED_DIAMOND.get())
                                        .define('X', STORAGE_MODULE2.get())
                                        .unlockedBy("storage", has(STORAGE_MODULE2.get())),
                                "tCt", "gXg", "QRQ"),
                Dob.itemBuilder(STORAGE_MODULE6)
        );
    }
}
