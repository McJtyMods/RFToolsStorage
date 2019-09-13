package mcjty.rftoolsstorage.setup;


import mcjty.lib.blocks.BaseBlockItem;
import mcjty.lib.container.GenericContainer;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.blocks.ModBlocks;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageBlock;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageTileEntity;
import mcjty.rftoolsstorage.modules.modularstorage.items.StorageModuleItem;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RFToolsStorage.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Registration {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new ModularStorageBlock());
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        Item.Properties properties = new Item.Properties().group(RFToolsStorage.setup.getTab());
        event.getRegistry().register(new StorageModuleItem(StorageModuleItem.STORAGE_TIER1));
        event.getRegistry().register(new StorageModuleItem(StorageModuleItem.STORAGE_TIER2));
        event.getRegistry().register(new StorageModuleItem(StorageModuleItem.STORAGE_TIER3));
        event.getRegistry().register(new StorageModuleItem(StorageModuleItem.STORAGE_TIER4));
        event.getRegistry().register(new StorageModuleItem(StorageModuleItem.STORAGE_REMOTE));
        event.getRegistry().register(new BaseBlockItem(ModBlocks.MODULAR_STORAGE, properties));
    }

    @SubscribeEvent
    public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().register(TileEntityType.Builder.create(ModularStorageTileEntity::new, ModBlocks.MODULAR_STORAGE).build(null).setRegistryName(ModularStorageBlock.REGNAME));
    }

    @SubscribeEvent
    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().register(GenericContainer.createContainerType(ModularStorageBlock.REGNAME));
    }

}
