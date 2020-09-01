package mcjty.rftoolsstorage.datagen;

import mcjty.lib.datagen.BaseLootTableProvider;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageModule;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerModule;
import net.minecraft.data.DataGenerator;

public class LootTables extends BaseLootTableProvider {

    public LootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        lootTables.put(ModularStorageModule.MODULAR_STORAGE.get(), createStandardTable("storage", ModularStorageModule.MODULAR_STORAGE.get()));
        lootTables.put(StorageScannerModule.STORAGE_SCANNER.get(), createStandardTable("storage", StorageScannerModule.STORAGE_SCANNER.get()));

    }
}
