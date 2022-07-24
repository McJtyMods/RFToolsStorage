package mcjty.rftoolsstorage.compat.jei;

import mcjty.rftoolsstorage.modules.scanner.StorageScannerModule;
import mcjty.rftoolsstorage.modules.scanner.blocks.RemoteStorageScannerContainer;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class RemoteStorageScannerRecipeTransferHandler implements IRecipeTransferHandler<RemoteStorageScannerContainer, CraftingRecipe> {

    public static void register(IRecipeTransferRegistration transferRegistry) {
        transferRegistry.addRecipeTransferHandler(new RemoteStorageScannerRecipeTransferHandler(), RecipeTypes.CRAFTING);
    }

    @Nonnull
    @Override
    public Class<RemoteStorageScannerContainer> getContainerClass() {
        return RemoteStorageScannerContainer.class;
    }

    @Override
    public Optional<MenuType<RemoteStorageScannerContainer>> getMenuType() {
        return Optional.of(StorageScannerModule.CONTAINER_STORAGE_SCANNER_REMOTE.get());
    }

    @Override
    public RecipeType<CraftingRecipe> getRecipeType() {
        return null;
    }

    @Override
    @Nullable
    public IRecipeTransferError transferRecipe(RemoteStorageScannerContainer container, CraftingRecipe recipe, IRecipeSlotsView recipeLayout, Player player, boolean maxTransfer, boolean doTransfer) {
        BlockEntity inventory = container.getTe();
        BlockPos pos = inventory.getBlockPos();
        List<IRecipeSlotView> slotViews = recipeLayout.getSlotViews();

        if (doTransfer) {
            RFToolsStorageJeiPlugin.transferRecipe(slotViews, pos);
        }

        return null;
    }
}
