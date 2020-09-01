package mcjty.rftoolsstorage.datagen;

import mcjty.lib.crafting.CopyNBTRecipeBuilder;
import mcjty.lib.datagen.BaseRecipeProvider;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageModule;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerModule;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class Recipes extends BaseRecipeProvider {

    public Recipes(DataGenerator generatorIn) {
        super(generatorIn);
        add('C', Tags.Items.CHESTS);
        add('q', Items.QUARTZ);
        add('Q', Items.QUARTZ_BLOCK);
        add('F', VariousModule.MACHINE_FRAME.get());
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        build(consumer, ShapedRecipeBuilder.shapedRecipe(ModularStorageModule.STORAGE_MODULE0.get())
                        .key('g', Items.GOLD_NUGGET)
                        .addCriterion("redstone", hasItem(Items.REDSTONE)),
                " C ", "gig", "qrq");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(ModularStorageModule.STORAGE_MODULE1.get())
                        .key('g', Items.GOLD_INGOT)
                        .key('X', ModularStorageModule.STORAGE_MODULE0.get())
                        .addCriterion("storage", hasItem(ModularStorageModule.STORAGE_MODULE0.get())),
                " C ", "gXg", "qrq");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(ModularStorageModule.STORAGE_MODULE2.get())
                        .key('g', Items.GOLD_BLOCK)
                        .key('X', ModularStorageModule.STORAGE_MODULE1.get())
                        .addCriterion("storage", hasItem(ModularStorageModule.STORAGE_MODULE1.get())),
                " C ", "gXg", "QRQ");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(ModularStorageModule.STORAGE_MODULE3.get())
                        .key('g', Items.DIAMOND_BLOCK)
                        .key('t', VariousModule.INFUSED_DIAMOND.get())
                        .key('X', ModularStorageModule.STORAGE_MODULE2.get())
                        .addCriterion("storage", hasItem(ModularStorageModule.STORAGE_MODULE2.get())),
                "tCt", "gXg", "QRQ");

        build(consumer, ShapedRecipeBuilder.shapedRecipe(ModularStorageModule.MODULAR_STORAGE.get())
                        .addCriterion("frame", hasItem(VariousModule.MACHINE_FRAME.get())),
                "rCr", "qFq", "rqr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(StorageScannerModule.STORAGE_SCANNER.get())
                        .key('g', Items.GOLD_INGOT)
                        .addCriterion("frame", hasItem(VariousModule.MACHINE_FRAME.get())),
                "ToT", "gFg", "ToT");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(StorageScannerModule.STORAGECONTROL_MODULE.get())
                        .key('X', Items.CRAFTING_TABLE)
                        .addCriterion("ingot", hasItem(Items.IRON_INGOT)),
                " X ", "rir", " X ");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(StorageScannerModule.DUMP_MODULE.get())
                        .key('X', ItemTags.WOODEN_BUTTONS)
                        .addCriterion("ingot", hasItem(Items.IRON_INGOT)),
                " X ", "rir", " X ");
    }
}
