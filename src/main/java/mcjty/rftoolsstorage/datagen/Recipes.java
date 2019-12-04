package mcjty.rftoolsstorage.datagen;

import mcjty.lib.crafting.CopyNBTRecipeBuilder;
import mcjty.lib.datagen.BaseRecipeProvider;
import mcjty.rftoolsbase.modules.various.VariousSetup;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageSetup;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerSetup;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Items;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class Recipes extends BaseRecipeProvider {

    public Recipes(DataGenerator generatorIn) {
        super(generatorIn);
        add('C', Tags.Items.CHESTS);
        add('q', Items.QUARTZ);
        add('Q', Items.QUARTZ_BLOCK);
        add('F', VariousSetup.MACHINE_FRAME.get());
        group("rftools");
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        build(consumer, ShapedRecipeBuilder.shapedRecipe(ModularStorageSetup.STORAGE_MODULE0.get())
                        .key('g', Items.GOLD_NUGGET)
                        .addCriterion("redstone", InventoryChangeTrigger.Instance.forItems(Items.REDSTONE)),
                " C ", "gig", "qrq");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(ModularStorageSetup.STORAGE_MODULE1.get())
                        .key('g', Items.GOLD_INGOT)
                        .key('X', ModularStorageSetup.STORAGE_MODULE0.get())
                        .addCriterion("storage", InventoryChangeTrigger.Instance.forItems(ModularStorageSetup.STORAGE_MODULE0.get())),
                " C ", "gXg", "qrq");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(ModularStorageSetup.STORAGE_MODULE2.get())
                        .key('g', Items.GOLD_BLOCK)
                        .key('X', ModularStorageSetup.STORAGE_MODULE1.get())
                        .addCriterion("storage", InventoryChangeTrigger.Instance.forItems(ModularStorageSetup.STORAGE_MODULE1.get())),
                " C ", "gXg", "QRQ");
        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(ModularStorageSetup.STORAGE_MODULE3.get())
                        .key('g', Items.DIAMOND_BLOCK)
                        .key('t', VariousSetup.INFUSED_DIAMOND.get())
                        .key('X', ModularStorageSetup.STORAGE_MODULE2.get())
                        .addCriterion("storage", InventoryChangeTrigger.Instance.forItems(ModularStorageSetup.STORAGE_MODULE2.get())),
                "tCt", "gXg", "QRQ");

        build(consumer, ShapedRecipeBuilder.shapedRecipe(ModularStorageSetup.MODULAR_STORAGE.get())
                        .addCriterion("frame", InventoryChangeTrigger.Instance.forItems(VariousSetup.MACHINE_FRAME.get())),
                "rCr", "qFq", "rqr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(StorageScannerSetup.STORAGE_SCANNER.get())
                        .key('g', Items.GOLD_INGOT)
                        .addCriterion("frame", InventoryChangeTrigger.Instance.forItems(VariousSetup.MACHINE_FRAME.get())),
                "ToT", "gFg", "ToT");
    }
}
