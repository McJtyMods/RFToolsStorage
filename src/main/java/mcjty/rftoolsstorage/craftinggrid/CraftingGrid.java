package mcjty.rftoolsstorage.craftinggrid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class CraftingGrid {

    private final CraftingGridInventory craftingGridInventory = new CraftingGridInventory();
    private final RFCraftingRecipe[] recipes = new RFCraftingRecipe[6];

    public CraftingGrid() {
        for (int i = 0 ; i < 6 ; i++) {
            recipes[i] = new RFCraftingRecipe();
        }
    }

    public CraftingGridInventory getCraftingGridInventory() {
        return craftingGridInventory;
    }

    public RFCraftingRecipe getRecipe(int index) {
        return recipes[index];
    }

    public RFCraftingRecipe getActiveRecipe() {
        RFCraftingRecipe recipe = new RFCraftingRecipe();
        recipe.setRecipe(craftingGridInventory.getIngredients(), craftingGridInventory.getResult());
        return recipe;
    }

    public void setRecipe(int index, ItemStack[] stacks) {
        RFCraftingRecipe recipe = recipes[index];
        recipe.setResult(stacks[0]);
        for (int i = 0 ; i < 9 ; i++) {
            recipe.getInventory().setItem(i, stacks[i+1]);
        }
    }

    public void storeRecipe(int index) {
        RFCraftingRecipe recipe = getRecipe(index);
        recipe.setRecipe(craftingGridInventory.getIngredients(), craftingGridInventory.getResult());
    }

    public void selectRecipe(int index) {
        RFCraftingRecipe recipe = getRecipe(index);
        craftingGridInventory.setStackInSlot(CraftingGridInventory.SLOT_GHOSTOUTPUT, recipe.getResult());
        for (int i = 0 ; i < 9 ; i++) {
            craftingGridInventory.setStackInSlot(i+CraftingGridInventory.SLOT_GHOSTINPUT, recipe.getInventory().getItem(i));
        }
    }

    public CompoundTag writeToNBT() {
        CompoundTag tagCompound = new CompoundTag();

        ListTag bufferTagList = new ListTag();
        for (int i = 0 ; i < craftingGridInventory.getSlots() ; i++) {
            CompoundTag CompoundNBT = new CompoundTag();
            ItemStack stack = craftingGridInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                stack.save(CompoundNBT);
            }
            bufferTagList.add(CompoundNBT);
        }
        tagCompound.put("grid", bufferTagList);

        ListTag recipeTagList = new ListTag();
        for (RFCraftingRecipe recipe : recipes) {
            CompoundTag CompoundNBT = new CompoundTag();
            recipe.writeToNBT(CompoundNBT);
            recipeTagList.add(CompoundNBT);
        }
        tagCompound.put("recipes", recipeTagList);

        return tagCompound;
    }

    public void readFromNBT(CompoundTag tagCompound) {
        if (tagCompound == null) {
            return;
        }
        ListTag bufferTagList = tagCompound.getList("grid", Tag.TAG_COMPOUND);
        for (int i = 0 ; i < craftingGridInventory.getSlots() ; i++) {
            CompoundTag CompoundNBT = bufferTagList.getCompound(i);
            craftingGridInventory.setStackInSlot(i, ItemStack.of(CompoundNBT));
        }

        ListTag recipeTagList = tagCompound.getList("recipes", Tag.TAG_COMPOUND);
        for (int i = 0 ; i < recipeTagList.size() ; i++) {
            recipes[i] = new RFCraftingRecipe();
            CompoundTag CompoundNBT = recipeTagList.getCompound(i);
            recipes[i].readFromNBT(CompoundNBT);
        }
    }
}
