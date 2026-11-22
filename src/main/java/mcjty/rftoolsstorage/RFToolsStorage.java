package mcjty.rftoolsstorage;

import mcjty.lib.modules.Modules;
import mcjty.rftoolsbase.api.infoscreen.CapabilityInformationScreenInfo;
import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerModule;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageModule;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerModule;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import mcjty.rftoolsstorage.setup.Config;
import mcjty.rftoolsstorage.setup.ModSetup;
import mcjty.rftoolsstorage.setup.RFToolsStorageMessages;
import mcjty.rftoolsstorage.setup.Registration;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.item.Item;
import java.util.function.Function;

public class RFToolsStorage implements ModInitializer {

    public static final String MODID = "rftoolsstorage";
    public static final ModSetup setup = new ModSetup();
    public static final Modules modules = new Modules();

    @Override
    public void onInitialize() {
        modules.register(new CraftingManagerModule());
        modules.register(new ModularStorageModule());
        modules.register(new StorageScannerModule());
        Config.register(modules);
        Registration.register();
        RFToolsStorageMessages.registerMessages();
        setup.init();
        modules.init();
        setup.registerBlockCapabilities(Registration.RBLOCKS);
        CapabilityInformationScreenInfo.INFORMATION_SCREEN_INFO_CAPABILITY.registerForBlocks(
                (level, pos, state, blockEntity, direction) -> blockEntity instanceof StorageScannerTileEntity scanner
                        ? scanner.getInfoScreenInfo() : null, StorageScannerModule.STORAGE_SCANNER.block().get());
    }

    public static <T extends Item> Function<Item.Properties, T> tab(Function<Item.Properties, T> factory) {
        return properties -> setup.tab(() -> factory.apply(properties)).get();
    }
}
