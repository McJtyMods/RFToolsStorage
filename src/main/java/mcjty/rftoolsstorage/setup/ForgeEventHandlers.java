package mcjty.rftoolsstorage.setup;

import mcjty.rftoolsstorage.commands.ModCommands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeEventHandlers {

    @SubscribeEvent
    public void serverLoad(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

}
