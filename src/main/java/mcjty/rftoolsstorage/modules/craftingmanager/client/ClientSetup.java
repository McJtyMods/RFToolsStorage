package mcjty.rftoolsstorage.modules.craftingmanager.client;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ClientSetup {
    public static void modelInit(EntityRenderersEvent.RegisterRenderers event) {
        CraftingManagerRenderer.register(event);
    }
}
