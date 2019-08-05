package mcjty.rftoolsstorage.setup;


import mcjty.lib.McJtyLib;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.blocks.ModBlocks;
import mcjty.rftoolsstorage.blocks.basic.GuiModularStorage;
import mcjty.rftoolsstorage.blocks.basic.ModularStorageContainer;
import mcjty.rftoolsstorage.blocks.basic.ModularStorageTileEntity;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RFToolsStorage.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistration {

    @SubscribeEvent
    public static void init(FMLClientSetupEvent e) {
        ScreenManager.IScreenFactory<ModularStorageContainer, GuiModularStorage> factory = (container, inventory, title) -> {
            TileEntity te = McJtyLib.proxy.getClientWorld().getTileEntity(container.getPos());
            return Tools.safeMap(te, (ModularStorageTileEntity i) -> new GuiModularStorage(i, container, inventory), "Invalid tile entity!");
        };
        ScreenManager.registerFactory(ModBlocks.CONTAINER_MODULAR_STORAGE, factory);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
    }
}
