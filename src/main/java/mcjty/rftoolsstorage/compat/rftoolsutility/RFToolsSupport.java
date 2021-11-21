package mcjty.rftoolsstorage.compat.rftoolsutility;

import mcjty.rftoolsbase.api.screens.IScreenModuleRegistry;
import mcjty.rftoolsstorage.modules.scanner.items.StorageControlScreenModule;

import javax.annotation.Nullable;
import java.util.function.Function;

public class RFToolsSupport {

    public static class GetScreenModuleRegistry implements Function<IScreenModuleRegistry, Void> {
        @Nullable
        @Override
        public Void apply(IScreenModuleRegistry manager) {
            manager.registerModuleDataFactory(StorageControlScreenModule.ModuleDataStacks.ID, StorageControlScreenModule.ModuleDataStacks::new);
            return null;
        }
    }
}
