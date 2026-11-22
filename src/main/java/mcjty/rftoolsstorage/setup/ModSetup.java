package mcjty.rftoolsstorage.setup;

import mcjty.lib.setup.DefaultModSetup;
import mcjty.rftoolsstorage.commands.ModCommands;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;

public class ModSetup extends DefaultModSetup {

    public boolean xnet;

    @Override
    public void init() {
        super.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> ModCommands.register(dispatcher));
        CommandHandler.registerCommands();
    }

    @Override
    protected void setupModCompat() {
        xnet = FabricLoader.getInstance().isModLoaded("xnet");
    }
}
