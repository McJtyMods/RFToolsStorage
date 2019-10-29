package mcjty.rftoolsstorage.datagen;

import mcjty.lib.crafting.CopyNBTRecipeBuilder;
import mcjty.lib.datagen.BaseRecipeProvider;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageSetup;
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
        group("rftools");
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        // @todo use infused diamond!

        build(consumer, ShapedRecipeBuilder.shapedRecipe(ModularStorageSetup.STORAGE_MODULE0)
                        .key('g', Items.GOLD_NUGGET)
                        .addCriterion("redstone", InventoryChangeTrigger.Instance.forItems(Items.REDSTONE)),
                " C ", "gig", "qrq");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(ModularStorageSetup.STORAGE_MODULE1)
                        .key('g', Items.GOLD_INGOT)
                        .key('F', ModularStorageSetup.STORAGE_MODULE0)
                        .addCriterion("storage", InventoryChangeTrigger.Instance.forItems(ModularStorageSetup.STORAGE_MODULE0)),
                " C ", "gFg", "qrq");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(ModularStorageSetup.STORAGE_MODULE2)
                        .key('g', Items.GOLD_BLOCK)
                        .key('F', ModularStorageSetup.STORAGE_MODULE1)
                        .addCriterion("storage", InventoryChangeTrigger.Instance.forItems(ModularStorageSetup.STORAGE_MODULE1)),
                " C ", "gFg", "QRQ");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(ModularStorageSetup.STORAGE_MODULE3)
                        .key('g', Items.DIAMOND_BLOCK)
                        .key('t', Items.GHAST_TEAR)
                        .key('F', ModularStorageSetup.STORAGE_MODULE2)
                        .addCriterion("storage", InventoryChangeTrigger.Instance.forItems(ModularStorageSetup.STORAGE_MODULE2)),
                "tCt", "gFg", "QRQ");
    }
}
