package mcjty.rftoolsstorage.setup;

import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultModSetup;
import mcjty.rftoolsstorage.network.RFToolsStorageMessages;
import mcjty.rftoolsstorage.storage.ClientStorageHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModSetup extends DefaultModSetup {

    public ClientStorageHolder clientStorageHolder = new ClientStorageHolder();

    public ModSetup() {
        createTab("rftoolsstorage", () -> new ItemStack(Items.CHEST));
    }   // @todo

    @Override
    public void init(FMLCommonSetupEvent e) {
        super.init(e);
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        CommandHandler.registerCommands();
        RFToolsStorageMessages.registerMessages("rftoolsstorage");
    }

    @Override
    protected void setupModCompat() {
        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();
//        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "mcjty.rftools.compat.theoneprobe.TheOneProbeSupport");
    }
}
