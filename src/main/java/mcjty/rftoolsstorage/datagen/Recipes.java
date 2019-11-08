package mcjty.rftoolsstorage.datagen;

import mcjty.lib.crafting.CopyNBTRecipeBuilder;
import mcjty.lib.datagen.BaseRecipeProvider;
import mcjty.rftoolsbase.blocks.ModBlocks;
import mcjty.rftoolsbase.items.ModItems;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageSetup;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerSetup;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
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
        add('F', ModItems.MACHINE_FRAME);
        group("rftools");
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        build(consumer, ShapedRecipeBuilder.shapedRecipe(ModularStorageSetup.STORAGE_MODULE0)
                        .key('g', Items.GOLD_NUGGET)
                        .addCriterion("redstone", InventoryChangeTrigger.Instance.forItems(Items.REDSTONE)),
                " C ", "gig", "qrq");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(ModularStorageSetup.STORAGE_MODULE1)
                        .key('g', Items.GOLD_INGOT)
                        .key('X', ModularStorageSetup.STORAGE_MODULE0)
                        .addCriterion("storage", InventoryChangeTrigger.Instance.forItems(ModularStorageSetup.STORAGE_MODULE0)),
                " C ", "gXg", "qrq");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(ModularStorageSetup.STORAGE_MODULE2)
                        .key('g', Items.GOLD_BLOCK)
                        .key('X', ModularStorageSetup.STORAGE_MODULE1)
                        .addCriterion("storage", InventoryChangeTrigger.Instance.forItems(ModularStorageSetup.STORAGE_MODULE1)),
                " C ", "gXg", "QRQ");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(ModularStorageSetup.STORAGE_MODULE3)
                        .key('g', Items.DIAMOND_BLOCK)
                        .key('t', ModItems.INFUSED_DIAMOND)
                        .key('X', ModularStorageSetup.STORAGE_MODULE2)
                        .addCriterion("storage", InventoryChangeTrigger.Instance.forItems(ModularStorageSetup.STORAGE_MODULE2)),
                "tCt", "gXg", "QRQ");

        build(consumer, ShapedRecipeBuilder.shapedRecipe(ModularStorageSetup.MODULAR_STORAGE)
                        .addCriterion("frame", InventoryChangeTrigger.Instance.forItems(ModItems.MACHINE_FRAME)),
                "rCr", "qFq", "rqr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(StorageScannerSetup.STORAGE_SCANNER)
                        .key('g', Items.GOLD_INGOT)
                        .addCriterion("frame", InventoryChangeTrigger.Instance.forItems(ModItems.MACHINE_FRAME)),
                "ToT", "gFg", "ToT");
    }
}
