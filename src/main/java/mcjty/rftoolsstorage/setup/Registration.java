package mcjty.rftoolsstorage.setup;


import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageSetup;
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
        ModularStorageSetup.registerBlocks(event);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        ModularStorageSetup.registerItems(event);
    }

    @SubscribeEvent
    public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
        ModularStorageSetup.registerTiles(event);
    }

    @SubscribeEvent
    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event) {
        ModularStorageSetup.registerContainers(event);
    }

}
