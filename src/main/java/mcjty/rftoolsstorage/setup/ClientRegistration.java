package mcjty.rftoolsstorage.setup;


import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.blocks.ModBlocks;
import mcjty.rftoolsstorage.modules.modularstorage.client.GuiModularStorage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RFToolsStorage.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistration {

    @SubscribeEvent
    public static void init(FMLClientSetupEvent e) {
        GenericGuiContainer.register(ModBlocks.CONTAINER_MODULAR_STORAGE, GuiModularStorage::new);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
    }
}
