package mcjty.rftoolsstorage.setup;

import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultModSetup;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.compat.rftoolsutility.RFToolsSupport;
import mcjty.rftoolsstorage.compat.xnet.XNetSupport;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageModule;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModSetup extends DefaultModSetup {

    public boolean xnet = false;

    public ModSetup() {
        createTab(RFToolsStorage.MODID, "rftoolsstorage", () -> new ItemStack(ModularStorageModule.STORAGE_MODULE0.get()));
    }

    @Override
    public void init(FMLCommonSetupEvent e) {
        super.init(e);
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        e.enqueueWork(() -> {
            CommandHandler.registerCommands();
        });
        RFToolsStorageMessages.registerMessages("rftoolsstorage");
    }

    @Override
    protected void setupModCompat() {
        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();
        xnet = ModList.get().isLoaded("xnet");
        if (xnet) {
            Logging.log("RFTools Storage Detected XNet: enabling support");
            InterModComms.sendTo("xnet", "getXNet", XNetSupport.GetXNet::new);
        }
        InterModComms.sendTo("rftoolsutility", "getScreenModuleRegistry", RFToolsSupport.GetScreenModuleRegistry::new);
    }
}
