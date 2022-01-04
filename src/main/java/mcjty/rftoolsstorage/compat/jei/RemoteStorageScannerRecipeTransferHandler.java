package mcjty.rftoolsstorage.compat.jei;

import mcjty.rftoolsstorage.modules.scanner.blocks.RemoteStorageScannerContainer;
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

public class RemoteStorageScannerRecipeTransferHandler implements IRecipeTransferHandler<RemoteStorageScannerContainer, CraftingRecipe> {

    public static void register(IRecipeTransferRegistration transferRegistry) {
        transferRegistry.addRecipeTransferHandler(new RemoteStorageScannerRecipeTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
    }

    @Nonnull
    @Override
    public Class<RemoteStorageScannerContainer> getContainerClass() {
        return RemoteStorageScannerContainer.class;
    }

    @Override
    public Class<CraftingRecipe> getRecipeClass() {
        return CraftingRecipe.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(RemoteStorageScannerContainer container, CraftingRecipe recipe, IRecipeLayout recipeLayout, Player player, boolean maxTransfer, boolean doTransfer) {
        Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = recipeLayout.getItemStacks().getGuiIngredients();

        BlockEntity inventory = container.getTe();
        BlockPos pos = inventory.getBlockPos();

        if (doTransfer) {
            RFToolsStorageJeiPlugin.transferRecipe(guiIngredients, pos);
        }

        return null;
    }
}
