package mcjty.rftoolsstorage.setup;

import mcjty.rftoolsstorage.commands.ModCommands;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class ForgeEventHandlers {

    @SubscribeEvent
    public void serverLoad(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

}
