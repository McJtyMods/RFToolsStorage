package mcjty.rftoolsstorage.modules.craftingmanager.client;

import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerModule;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.event.EntityRenderersEvent;

public class ClientSetup {
    public static void initClient() {
        ItemBlockRenderTypes.setRenderLayer(CraftingManagerModule.CRAFTING_MANAGER.get(), RenderType.cutout());
    }

    public static void modelInit(EntityRenderersEvent.RegisterRenderers event) {
        CraftingManagerRenderer.register(event);
    }
}
