package mcjty.rftoolsstorage;

import mcjty.lib.modules.Modules;
import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerModule;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageModule;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerModule;
import mcjty.rftoolsstorage.setup.Config;
import mcjty.rftoolsstorage.setup.ModSetup;
import mcjty.rftoolsstorage.setup.Registration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RFToolsStorage.MODID)
public class RFToolsStorage {

    public static final String MODID = "rftoolsstorage";

    @SuppressWarnings("PublicField")
    public static final ModSetup setup = new ModSetup();
    private final Modules modules = new Modules();

    public static RFToolsStorage instance;

    public RFToolsStorage() {
        instance = this;
        setupModules();

        Config.register(modules);
        Registration.register();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(setup::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(modules::init);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(modules::initClient);
        });
    }

    private void setupModules() {
        modules.register(new CraftingManagerModule());
        modules.register(new ModularStorageModule());
        modules.register(new StorageScannerModule());
    }
}