package mcjty.rftoolsstorage.setup;


import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerSetup;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageSetup;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerSetup;
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
        StorageScannerSetup.registerBlocks(event);
        CraftingManagerSetup.registerBlocks(event);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        ModularStorageSetup.registerItems(event);
        StorageScannerSetup.registerItems(event);
        CraftingManagerSetup.registerItems(event);
    }

    @SubscribeEvent
    public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
        ModularStorageSetup.registerTiles(event);
        StorageScannerSetup.registerTiles(event);
        CraftingManagerSetup.registerTiles(event);
    }

    @SubscribeEvent
    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event) {
        ModularStorageSetup.registerContainers(event);
        StorageScannerSetup.registerContainers(event);
        CraftingManagerSetup.registerContainers(event);
    }

}
