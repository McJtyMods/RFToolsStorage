package mcjty.rftoolsstorage.modules.craftingmanager.client;

import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerModule;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.client.event.ModelRegistryEvent;

public class ClientSetup {
    public static void initClient() {
        RenderTypeLookup.setRenderLayer(CraftingManagerModule.CRAFTING_MANAGER.get(), RenderType.cutout());
    }

    public static void modelInit(ModelRegistryEvent event) {
        CraftingManagerRenderer.register();
    }
}
