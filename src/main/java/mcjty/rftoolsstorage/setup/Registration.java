package mcjty.rftoolsstorage.setup;


import mcjty.lib.container.GenericContainer;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.blocks.ModBlocks;
import mcjty.rftoolsstorage.blocks.basic.ModularStorageBlock;
import mcjty.rftoolsstorage.blocks.basic.ModularStorageTileEntity;
import mcjty.rftoolsstorage.items.StorageModuleItem;
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
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new StorageModuleItem(StorageModuleItem.STORAGE_TIER1));
        event.getRegistry().register(new StorageModuleItem(StorageModuleItem.STORAGE_TIER2));
        event.getRegistry().register(new StorageModuleItem(StorageModuleItem.STORAGE_TIER3));
        event.getRegistry().register(new StorageModuleItem(StorageModuleItem.STORAGE_TIER4));
        event.getRegistry().register(new StorageModuleItem(StorageModuleItem.STORAGE_REMOTE));
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
