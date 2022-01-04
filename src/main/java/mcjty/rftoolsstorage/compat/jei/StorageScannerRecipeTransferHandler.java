package mcjty.rftoolsstorage.compat.jei;

import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerContainer;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class StorageScannerRecipeTransferHandler implements IRecipeTransferHandler<StorageScannerContainer, CraftingRecipe> {

    public static void register(IRecipeTransferRegistration transferRegistry) {
        transferRegistry.addRecipeTransferHandler(new StorageScannerRecipeTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
    }

    @Nonnull
    @Override
    public Class<StorageScannerContainer> getContainerClass() {
        return StorageScannerContainer.class;
    }

    @Override
    public Class<CraftingRecipe> getRecipeClass() {
        return CraftingRecipe.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(StorageScannerContainer container, CraftingRecipe recipe, IRecipeLayout recipeLayout, Player player, boolean maxTransfer, boolean doTransfer) {
        Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = recipeLayout.getItemStacks().getGuiIngredients();

        BlockEntity inventory = container.getTe();
        BlockPos pos = inventory.getBlockPos();

        if (doTransfer) {
            RFToolsStorageJeiPlugin.transferRecipe(guiIngredients, pos);
        }

        return null;
    }
}
