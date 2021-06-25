package mcjty.rftoolsstorage.modules.modularstorage.client;

import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageModule;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;

public class ClientSetup {
    public static void initClient() {
        RenderTypeLookup.setRenderLayer(ModularStorageModule.MODULAR_STORAGE.get(), RenderType.cutout());
    }
}
