package mcjty.rftoolsstorage.setup;

import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultModSetup;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsstorage.compat.rftoolsutility.RFToolsSupport;
import mcjty.rftoolsstorage.compat.xnet.XNetSupport;
import net.neoforged.neoforge.common.MinecraftForge;
import net.neoforged.neoforge.fml.InterModComms;
import net.neoforged.neoforge.fml.ModList;
import net.neoforged.neoforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModSetup extends DefaultModSetup {

    public boolean xnet = false;

    @Override
    public void init(FMLCommonSetupEvent e) {
        super.init(e);
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        e.enqueueWork(() -> {
            CommandHandler.registerCommands();
        });
        RFToolsStorageMessages.registerMessages();
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
