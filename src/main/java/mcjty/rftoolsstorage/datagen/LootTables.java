package mcjty.rftoolsstorage.datagen;

import mcjty.lib.datagen.BaseLootTableProvider;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageSetup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.LootTableProvider;

public class LootTables extends BaseLootTableProvider {

    public LootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        lootTables.put(ModularStorageSetup.MODULAR_STORAGE, createStandardTable("storage", ModularStorageSetup.MODULAR_STORAGE));

    }
}
