package mcjty.rftoolsstorage.modules.modularstorage;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.crafting.CopyNBTRecipeBuilder;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageBlock;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageTileEntity;
import mcjty.rftoolsstorage.modules.modularstorage.client.GuiModularStorage;
import mcjty.rftoolsstorage.modules.modularstorage.items.StorageModuleItem;
import mcjty.rftoolsstorage.setup.Config;
import mcjty.rftoolsstorage.setup.Registration;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import static mcjty.lib.datagen.DataGen.has;
import static mcjty.rftoolsstorage.RFToolsStorage.tab;
import static mcjty.rftoolsstorage.setup.Registration.*;

public class ModularStorageModule implements IModule {

    public static final RegistryObject<BaseBlock> MODULAR_STORAGE = BLOCKS.register("modular_storage", ModularStorageBlock::new);
    public static final RegistryObject<Item> MODULAR_STORAGE_ITEM = ITEMS.register("modular_storage", tab(() -> new BlockItem(MODULAR_STORAGE.get(), Registration.createStandardProperties())));
    public static final RegistryObject<BlockEntityType<?>> TYPE_MODULAR_STORAGE = TILES.register("modular_storage", () -> BlockEntityType.Builder.of(ModularStorageTileEntity::new, MODULAR_STORAGE.get()).build(null));
    public static final RegistryObject<MenuType<ModularStorageContainer>> CONTAINER_MODULAR_STORAGE = CONTAINERS.register("modular_storage", GenericContainer::createContainerType);

    public static final RegistryObject<StorageModuleItem> STORAGE_MODULE0 = ITEMS.register("storage_module0", tab(() -> new StorageModuleItem(StorageModuleItem.STORAGE_TIER1)));
    public static final RegistryObject<StorageModuleItem> STORAGE_MODULE1 = ITEMS.register("storage_module1", tab(() -> new StorageModuleItem(StorageModuleItem.STORAGE_TIER2)));
    public static final RegistryObject<StorageModuleItem> STORAGE_MODULE2 = ITEMS.register("storage_module2", tab(() -> new StorageModuleItem(StorageModuleItem.STORAGE_TIER3)));
    public static final RegistryObject<StorageModuleItem> STORAGE_MODULE3 = ITEMS.register("storage_module3", tab(() -> new StorageModuleItem(StorageModuleItem.STORAGE_TIER4)));
    public static final RegistryObject<StorageModuleItem> STORAGE_MODULE6 = ITEMS.register("storage_module6", () -> new StorageModuleItem(StorageModuleItem.STORAGE_REMOTE));   // @todo no tab yet

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

    @Override
    public void initDatagen(DataGen dataGen) {
        dataGen.add(
                Dob.blockBuilder(MODULAR_STORAGE)
                        .ironPickaxeTags()
                        .standardLoot(TYPE_MODULAR_STORAGE)
                        .blockState(DataGenHelper::generateModularStorage)
                        .shaped(builder -> builder
                                        .define('q', Items.QUARTZ)
                                        .define('C', Tags.Items.CHESTS)
                                        .define('F', VariousModule.MACHINE_FRAME.get())
                                        .unlockedBy("frame", has(VariousModule.MACHINE_FRAME.get())),
                                "rCr", "qFq", "rqr"),
                Dob.itemBuilder(STORAGE_MODULE0)
                        .shaped(builder -> builder
                                        .define('q', Items.QUARTZ)
                                        .define('C', Tags.Items.CHESTS)
                                        .define('g', Items.GOLD_NUGGET)
                                        .unlockedBy("redstone", has(Items.REDSTONE)),
                                " C ", "gig", "qrq"),
                Dob.itemBuilder(STORAGE_MODULE1)
                        .shapedNBT(builder -> builder
                                        .define('q', Items.QUARTZ)
                                        .define('C', Tags.Items.CHESTS)
                                        .define('g', Items.GOLD_INGOT)
                                        .define('X', STORAGE_MODULE0.get())
                                        .unlockedBy("storage", has(STORAGE_MODULE0.get())),
                                " C ", "gXg", "qrq"),
                Dob.itemBuilder(STORAGE_MODULE2)
                        .shapedNBT(builder -> builder
                                        .define('C', Tags.Items.CHESTS)
                                        .define('g', Items.GOLD_BLOCK)
                                        .define('X', STORAGE_MODULE1.get())
                                        .define('Q', Items.QUARTZ_BLOCK)
                                        .unlockedBy("storage", has(STORAGE_MODULE1.get())),
                                " C ", "gXg", "QRQ"),
                Dob.itemBuilder(STORAGE_MODULE3)
                        .shapedNBT(builder -> builder
                                        .define('C', Tags.Items.CHESTS)
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
