package mcjty.rftoolsstorage.compat.jei;

import mcjty.lib.varia.ItemStackList;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.setup.RFToolsStorageMessages;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

@JeiPlugin
public class RFToolsStorageJeiPlugin implements IModPlugin {

    public static void transferRecipe(Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients, BlockPos pos) {
        ItemStackList items = ItemStackList.create(10);
        for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : guiIngredients.entrySet()) {
            int recipeSlot = entry.getKey();
            List<ItemStack> allIngredients = entry.getValue().getAllIngredients();
            if (!allIngredients.isEmpty()) {
                items.set(recipeSlot, allIngredients.get(0));
            }
        }

        RFToolsStorageMessages.INSTANCE.sendToServer(new PacketSendRecipe(items, pos));
    }

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(RFToolsStorage.MODID, "rftoolsstorage");
    }

    @Override
    public void registerRecipeTransferHandlers(@Nonnull IRecipeTransferRegistration registration) {
        StorageRecipeTransferHandler.register(registration);
        StorageScannerRecipeTransferHandler.register(registration);
        RemoteStorageScannerRecipeTransferHandler.register(registration);
    }
}
