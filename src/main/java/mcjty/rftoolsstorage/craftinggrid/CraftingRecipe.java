package mcjty.rftoolsstorage.craftinggrid;

import mcjty.lib.McJtyLib;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.Optional;

public class CraftingRecipe {
    private CraftingInventory inv = new CraftingInventory(new Container(null, -1) {
        @Override
        public boolean stillValid(PlayerEntity playerIn) {
            return false;
        }
    }, 3, 3);
    private ItemStack result = ItemStack.EMPTY;

    private boolean recipePresent = false;
    private Optional<ICraftingRecipe> recipe = Optional.empty();

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

    public static Optional<ICraftingRecipe> findRecipe(World world, CraftingInventory inv) {
        return McJtyLib.proxy.getRecipeManager(world).getRecipeFor(IRecipeType.CRAFTING, inv, world);
    }

    public void readFromNBT(CompoundNBT tagCompound) {
        ListNBT nbtTagList = tagCompound.getList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < nbtTagList.size(); i++) {
            CompoundNBT CompoundNBT = nbtTagList.getCompound(i);
            inv.setItem(i, ItemStack.of(CompoundNBT));
        }
        CompoundNBT resultCompound = tagCompound.getCompound("Result");
        if (resultCompound != null) {
            result = ItemStack.of(resultCompound);
        } else {
            result = ItemStack.EMPTY;
        }
        keepOne = tagCompound.getBoolean("Keep");
        craftMode = CraftMode.values()[tagCompound.getByte("Int")];
        recipePresent = false;
    }

    public void writeToNBT(CompoundNBT tagCompound) {
        ListNBT nbtTagList = new ListNBT();
        for (int i = 0 ; i < 9 ; i++) {
            ItemStack stack = inv.getItem(i);
            CompoundNBT CompoundNBT = new CompoundNBT();
            if (!stack.isEmpty()) {
                stack.save(CompoundNBT);
            }
            nbtTagList.add(CompoundNBT);
        }
        CompoundNBT resultCompound = new CompoundNBT();
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

    public CraftingInventory getInventory() {
        return inv;
    }

    public void setResult(ItemStack result) {
        this.result = result;
    }

    public ItemStack getResult() {
        return result;
    }

    public Optional<ICraftingRecipe> getCachedRecipe(World world) {
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
