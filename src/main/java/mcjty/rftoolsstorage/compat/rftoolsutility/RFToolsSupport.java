package mcjty.rftoolsstorage.compat.rftoolsutility;

import mcjty.rftoolsbase.api.screens.IScreenModuleRegistry;
import mcjty.rftoolsbase.api.teleportation.ITeleportationManager;
import mcjty.rftoolsstorage.modules.scanner.items.StorageControlScreenModule;

public class RFToolsSupport {

    public static void register(ITeleportationManager teleportationManager, IScreenModuleRegistry registry) {
        registry.registerModuleDataFactory(StorageControlScreenModule.ModuleDataStacks.ID,
                StorageControlScreenModule.ModuleDataStacks::new);
    }
}
