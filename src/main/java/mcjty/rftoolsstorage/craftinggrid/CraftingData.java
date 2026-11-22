package mcjty.rftoolsstorage.craftinggrid;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import java.util.List;
import java.util.Optional;

public final class CraftingData {

    private CraftingData() {
    }

    public static ItemStack readStack(HolderLookup.Provider registries, CompoundTag tag) {
        return ItemStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag)
                .result().orElse(ItemStack.EMPTY);
    }

    public static CompoundTag writeStack(HolderLookup.Provider registries, ItemStack stack) {
        return ItemStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack)
                .getOrThrow().asCompound().orElseThrow();
    }

    public static List<Optional<Ingredient>> ingredients(CraftingRecipe recipe, List<ItemStack> template) {
        if (recipe instanceof ShapedRecipe shaped) {
            return shaped.getIngredients();
        }
        List<Ingredient> ingredients = recipe.placementInfo().ingredients();
        if (!ingredients.isEmpty()) {
            return ingredients.stream().map(Optional::of).toList();
        }
        return template.stream()
                .map(stack -> stack.isEmpty() ? Optional.<Ingredient>empty() : Optional.of(Ingredient.of(stack.getItem())))
                .toList();
    }
}
