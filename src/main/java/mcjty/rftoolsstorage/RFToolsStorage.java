package mcjty.rftoolsstorage;

import mcjty.lib.base.ModBase;
import mcjty.rftoolsstorage.modules.scanner.ScannerModuleRegistry;
import mcjty.rftoolsstorage.setup.Config;
import mcjty.rftoolsstorage.setup.ModSetup;
import mcjty.rftoolsstorage.setup.Registration;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RFToolsStorage.MODID)
public class RFToolsStorage implements ModBase {

    public static final String MODID = "rftoolsstorage";

    @SuppressWarnings("PublicField")
    public static ModSetup setup = new ModSetup();
    public static ScannerModuleRegistry screenModuleRegistry = new ScannerModuleRegistry();

    public static RFToolsStorage instance;

    public RFToolsStorage() {
        instance = this;

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);

        Registration.register();

        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLCommonSetupEvent event) -> setup.init(event));
        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLClientSetupEvent event) -> setup.initClient(event));
    }

    @Override
    public String getModId() {
        return MODID;
    }
}
