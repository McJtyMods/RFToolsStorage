package mcjty.rftoolsstorage.craftinggrid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RFCraftingRecipe {
    private final List<ItemStack> inv = new ArrayList<>(9);
    {
        for (int i = 0; i < 9; i++) {
            inv.add(ItemStack.EMPTY);
        }
    }
//    private final CraftingContainer inv = new TransientCraftingContainer(new AbstractContainerMenu(null, -1) {
//        @Override
//        public boolean stillValid(@Nonnull Player playerIn) {
//            return false;
//        }
//
//        @Override
//        public ItemStack quickMoveStack(Player player, int slot) {
//            return ItemStack.EMPTY;
//        }
//    }, 3, 3);
    private ItemStack result = ItemStack.EMPTY;

    private boolean recipePresent = false;
    private Optional<RecipeHolder<CraftingRecipe>> recipe = Optional.empty();

    private boolean keepOne = false;

    public enum CraftMode {
        EXT("Ext"),
        INT("Int"),
        EXTC("ExtC");

        private final String description;

        CraftMode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private CraftMode craftMode = CraftMode.EXT;

    public static Optional<RecipeHolder<CraftingRecipe>> findRecipe(Level world, CraftingInput inv) {
        return world.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, inv, world);
    }

    public void readFromNBT(CompoundTag tagCompound) {
        // @todo 1.21 data
//        ListTag nbtTagList = tagCompound.getList("Items", Tag.TAG_COMPOUND);
//        for (int i = 0; i < nbtTagList.size(); i++) {
//            CompoundTag CompoundNBT = nbtTagList.getCompound(i);
//            inv.setItem(i, ItemStack.of(CompoundNBT));
//        }
//        CompoundTag resultCompound = tagCompound.getCompound("Result");
//        result = ItemStack.of(resultCompound);
//        keepOne = tagCompound.getBoolean("Keep");
//        craftMode = CraftMode.values()[tagCompound.getByte("Int")];
//        recipePresent = false;
    }

    public void writeToNBT(CompoundTag tagCompound) {
        // @todo 1.21 data
//        ListTag nbtTagList = new ListTag();
//        for (int i = 0 ; i < 9 ; i++) {
//            ItemStack stack = inv.getItem(i);
//            CompoundTag CompoundNBT = new CompoundTag();
//            if (!stack.isEmpty()) {
//                stack.save(CompoundNBT);
//            }
//            nbtTagList.add(CompoundNBT);
//        }
//        CompoundTag resultCompound = new CompoundTag();
//        if (!result.isEmpty()) {
//            result.save(resultCompound);
//        }
//        tagCompound.put("Result", resultCompound);
//        tagCompound.put("Items", nbtTagList);
//        tagCompound.putBoolean("Keep", keepOne);
//        tagCompound.putByte("Int", (byte) craftMode.ordinal());
    }

    public void setRecipe(ItemStack[] items, ItemStack result) {
        for (int i = 0 ; i < 9 ; i++) {
            inv.set(i, items[i]);
        }
        this.result = result;
        recipePresent = false;
    }

    public List<ItemStack> getInventory() {
        return inv;
    }

    public void setResult(ItemStack result) {
        this.result = result;
    }

    public ItemStack getResult() {
        return result;
    }

    public Optional<RecipeHolder<CraftingRecipe>> getCachedRecipe(Level world) {
        if (!recipePresent) {
            recipePresent = true;
            recipe = findRecipe(world, CraftingInput.ofPositioned(3, 3, inv).input());
        }
        return recipe;
    }

    public boolean isKeepOne() {
        return keepOne;
    }

    public void setKeepOne(boolean keepOne) {
        this.keepOne = keepOne;
    }

    public CraftMode getCraftMode() {
        return craftMode;
    }

    public void setCraftMode(CraftMode craftMode) {
        this.craftMode = craftMode;
    }
}
