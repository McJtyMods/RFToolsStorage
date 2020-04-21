package mcjty.rftoolsstorage.modules.scanner;

import mcjty.lib.container.GenericContainer;
import mcjty.rftoolsbase.modules.tablet.items.TabletItem;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageSetup;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerBlock;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerContainer;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import mcjty.rftoolsstorage.modules.scanner.items.DumpModuleItem;
import mcjty.rftoolsstorage.modules.scanner.items.StorageControlModuleItem;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static mcjty.rftoolsstorage.RFToolsStorage.MODID;

public class StorageScannerSetup {

    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<TileEntityType<?>> TILES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, MODID);
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = new DeferredRegister<>(ForgeRegistries.CONTAINERS, MODID);

    public static void register() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<Block> STORAGE_SCANNER = BLOCKS.register("storage_scanner", StorageScannerBlock::new);
    public static final RegistryObject<Item> STORAGE_SCANNER_ITEM = ITEMS.register("storage_scanner", () -> new BlockItem(STORAGE_SCANNER.get(), RFToolsStorage.createStandardProperties()));
    public static final RegistryObject<TileEntityType<?>> TYPE_STORAGE_SCANNER = TILES.register("storage_scanner", () -> TileEntityType.Builder.create(StorageScannerTileEntity::new, STORAGE_SCANNER.get()).build(null));
    public static final RegistryObject<ContainerType<StorageScannerContainer>> CONTAINER_STORAGE_SCANNER = CONTAINERS.register("storage_scanner", GenericContainer::createContainerType);
    public static final RegistryObject<ContainerType<StorageScannerContainer>> CONTAINER_STORAGE_SCANNER_REMOTE = CONTAINERS.register("storage_scanner_remote", StorageScannerSetup::createProcessorRemote);

    public static final RegistryObject<Item> STORAGECONTROL_MODULE = ITEMS.register("storage_control_module", StorageControlModuleItem::new);
    public static final RegistryObject<Item> DUMP_MODULE = ITEMS.register("dump_module", DumpModuleItem::new);

    public static final RegistryObject<TabletItem> TABLET_SCANNER = ModularStorageSetup.ITEMS.register("tablet_scanner", TabletItem::new);

    public static ContainerType<StorageScannerContainer> createProcessorRemote() {
        ContainerType<StorageScannerContainer> containerType = IForgeContainerType.create((windowId, inv, data) -> {
            BlockPos pos = data.readBlockPos();
            DimensionType type = DimensionType.getById(data.readInt());

            StorageScannerTileEntity te = new StorageScannerTileEntity() {
                @Override
                public boolean isDummy() {
                    return true;
                }

                @Override
                public DimensionType getDimensionType() {
                    return type;
                }
            }; // Dummy tile entity
            te.setWorldAndPos(inv.player.getEntityWorld(), pos);    // Wrong world but doesn't really matter
            CompoundNBT compound = data.readCompoundTag();
            te.read(compound);

            StorageScannerContainer container = new StorageScannerContainer(CONTAINER_STORAGE_SCANNER_REMOTE.get(), windowId, pos, inv.player, te);
            container.setupInventories(new ItemStackHandler(StorageScannerContainer.SLOTS), inv);
            return container;
        });
        return containerType;
    }}
