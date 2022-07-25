package mcjty.rftoolsstorage.compat.jei;

import mcjty.lib.varia.ItemStackList;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.setup.RFToolsStorageMessages;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

@JeiPlugin
public class RFToolsStorageJeiPlugin implements IModPlugin {

    public static void transferRecipe(List<IRecipeSlotView> slotViews, BlockPos pos) {
        ItemStackList items = ItemStackList.create(10);
        for (int i = 0 ; i < 10 ; i++) {
            items.set(i, ItemStack.EMPTY);
        }
        for (int i = 0 ; i < slotViews.size() ; i++) {
            List<ITypedIngredient<?>> allIngredients = slotViews.get(i).getAllIngredients().collect(Collectors.toList());
            if (!allIngredients.isEmpty()) {
                ItemStack stack = allIngredients.get(0).getIngredient(VanillaTypes.ITEM_STACK).get();
                items.set(i, stack);
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
