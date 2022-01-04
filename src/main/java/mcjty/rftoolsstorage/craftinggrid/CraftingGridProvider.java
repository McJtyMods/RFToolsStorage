package mcjty.rftoolsstorage.craftinggrid;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public interface CraftingGridProvider {

    void setRecipe(int index, ItemStack[] stacks);

    void storeRecipe(int index);

    CraftingGrid getCraftingGrid();

    void markInventoryDirty();

    @Nonnull
    int[] craft(Player player, int n, boolean test);
}
