package mcjty.rftoolsstorage.craftinggrid;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;

public interface CraftingGridProvider {

    void setRecipe(int index, ItemStack[] stacks);

    void storeRecipe(int index);

    CraftingGrid getCraftingGrid();

    void markInventoryDirty();

    @Nonnull
    List<Pair<ItemStack, Integer>> craft(Player player, int n, boolean test);
}
