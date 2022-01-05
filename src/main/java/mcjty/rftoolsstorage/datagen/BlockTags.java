package mcjty.rftoolsstorage.datagen;

import mcjty.lib.datagen.BaseBlockTagsProvider;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerModule;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageModule;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerModule;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

public class BlockTags extends BaseBlockTagsProvider {

    public BlockTags(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, RFToolsStorage.MODID, helper);
    }

    @Override
    protected void addTags() {
        ironPickaxe(
                CraftingManagerModule.CRAFTING_MANAGER,
                ModularStorageModule.MODULAR_STORAGE,
                StorageScannerModule.STORAGE_SCANNER
        );
    }

    @Override
    @Nonnull
    public String getName() {
        return "RFToolsStorage Tags";
    }
}
