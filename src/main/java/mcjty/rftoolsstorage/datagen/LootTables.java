package mcjty.rftoolsstorage.datagen;

import mcjty.lib.datagen.BaseLootTableProvider;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageSetup;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerSetup;
import net.minecraft.data.DataGenerator;

public class LootTables extends BaseLootTableProvider {

    public LootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        lootTables.put(ModularStorageSetup.MODULAR_STORAGE.get(), createStandardTable("storage", ModularStorageSetup.MODULAR_STORAGE.get()));
        lootTables.put(StorageScannerSetup.STORAGE_SCANNER.get(), createStandardTable("storage", StorageScannerSetup.STORAGE_SCANNER.get()));

    }
}
