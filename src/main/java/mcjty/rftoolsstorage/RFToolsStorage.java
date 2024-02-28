package mcjty.rftoolsstorage;

import mcjty.lib.datagen.DataGen;
import mcjty.lib.modules.Modules;
import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerModule;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageModule;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerModule;
import mcjty.rftoolsstorage.setup.Config;
import mcjty.rftoolsstorage.setup.ModSetup;
import mcjty.rftoolsstorage.setup.Registration;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.api.distmarker.Dist;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.eventbus.api.IEventBus;
import net.neoforged.neoforge.fml.common.Mod;
import net.neoforged.neoforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.fml.loading.FMLEnvironment;

import java.util.function.Supplier;

@Mod(RFToolsStorage.MODID)
public class RFToolsStorage {

    public static final String MODID = "rftoolsstorage";

    @SuppressWarnings("PublicField")
    public static final ModSetup setup = new ModSetup();
    private final Modules modules = new Modules();

    public static RFToolsStorage instance;

    public RFToolsStorage() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        Dist dist = FMLEnvironment.dist;

        instance = this;
        setupModules(bus, dist);

        Config.register(bus, modules);
        Registration.register(bus);

        bus.addListener(setup::init);
        bus.addListener(modules::init);
        bus.addListener(this::onDataGen);

        if (dist.isClient()) {
            bus.addListener(modules::initClient);
        }
    }

    public static <T extends Item> Supplier<T> tab(Supplier<T> supplier) {
        return instance.setup.tab(supplier);
    }

    private void onDataGen(GatherDataEvent event) {
        DataGen datagen = new DataGen(MODID, event);
        modules.datagen(datagen);
        datagen.generate();
    }

    private void setupModules(IEventBus bus, Dist dist) {
        modules.register(new CraftingManagerModule(bus, dist));
        modules.register(new ModularStorageModule());
        modules.register(new StorageScannerModule());
    }
}