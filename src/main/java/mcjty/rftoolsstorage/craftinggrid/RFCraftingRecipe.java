package mcjty.rftoolsstorage.craftinggrid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Optional;

public class RFCraftingRecipe {
    private final CraftingContainer inv = new TransientCraftingContainer(new AbstractContainerMenu(null, -1) {
        @Override
        public boolean stillValid(@Nonnull Player playerIn) {
            return false;
        }

        @Override
        public ItemStack quickMoveStack(Player player, int slot) {
            return ItemStack.EMPTY;
        }
    }, 3, 3);
    private ItemStack result = ItemStack.EMPTY;

    private boolean recipePresent = false;
    private Optional<CraftingRecipe> recipe = Optional.empty();

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

    public static Optional<CraftingRecipe> findRecipe(Level world, CraftingContainer inv) {
        return world.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, inv, world);
    }

    public void readFromNBT(CompoundTag tagCompound) {
        ListTag nbtTagList = tagCompound.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < nbtTagList.size(); i++) {
            CompoundTag CompoundNBT = nbtTagList.getCompound(i);
            inv.setItem(i, ItemStack.of(CompoundNBT));
        }
        CompoundTag resultCompound = tagCompound.getCompound("Result");
        result = ItemStack.of(resultCompound);
        keepOne = tagCompound.getBoolean("Keep");
        craftMode = CraftMode.values()[tagCompound.getByte("Int")];
        recipePresent = false;
    }

    public void writeToNBT(CompoundTag tagCompound) {
        ListTag nbtTagList = new ListTag();
        for (int i = 0 ; i < 9 ; i++) {
            ItemStack stack = inv.getItem(i);
            CompoundTag CompoundNBT = new CompoundTag();
            if (!stack.isEmpty()) {
                stack.save(CompoundNBT);
            }
            nbtTagList.add(CompoundNBT);
        }
        CompoundTag resultCompound = new CompoundTag();
        if (!result.isEmpty()) {
            result.save(resultCompound);
        }
        tagCompound.put("Result", resultCompound);
        tagCompound.put("Items", nbtTagList);
        tagCompound.putBoolean("Keep", keepOne);
        tagCompound.putByte("Int", (byte) craftMode.ordinal());
    }

    public void setRecipe(ItemStack[] items, ItemStack result) {
        for (int i = 0 ; i < 9 ; i++) {
            inv.setItem(i, items[i]);
        }
        this.result = result;
        recipePresent = false;
    }

    public CraftingContainer getInventory() {
        return inv;
    }

    public void setResult(ItemStack result) {
        this.result = result;
    }

    public ItemStack getResult() {
        return result;
    }

    public Optional<CraftingRecipe> getCachedRecipe(Level world) {
        if (!recipePresent) {
            recipePresent = true;
            recipe = findRecipe(world, inv);
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
