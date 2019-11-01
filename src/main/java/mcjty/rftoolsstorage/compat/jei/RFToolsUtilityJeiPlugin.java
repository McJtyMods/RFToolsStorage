package mcjty.rftoolsstorage.compat.jei;

import mcjty.lib.varia.ItemStackList;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.network.RFToolsStorageMessages;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;

@JeiPlugin
public class RFToolsUtilityJeiPlugin implements IModPlugin {

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

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(RFToolsStorage.MODID, "rftoolsstorage");
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        StorageRecipeTransferHandler.register(registration);
    }
}
