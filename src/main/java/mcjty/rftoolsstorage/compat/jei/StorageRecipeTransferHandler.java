package mcjty.rftoolsstorage.compat.jei;

import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class StorageRecipeTransferHandler implements IRecipeTransferHandler<ModularStorageContainer> {

    public static void register(IRecipeTransferRegistration transferRegistry) {
        transferRegistry.addRecipeTransferHandler(new StorageRecipeTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
    }

    @Override
    public Class<ModularStorageContainer> getContainerClass() {
        return ModularStorageContainer.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull ModularStorageContainer container, @Nonnull IRecipeLayout recipeLayout, @Nonnull PlayerEntity player, boolean maxTransfer, boolean doTransfer) {
        Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = recipeLayout.getItemStacks().getGuiIngredients();

        TileEntity inventory = container.getTe();
        BlockPos pos = inventory.getBlockPos();

        if (doTransfer) {
            RFToolsStorageJeiPlugin.transferRecipe(guiIngredients, pos);
        }

        return null;
    }
}
