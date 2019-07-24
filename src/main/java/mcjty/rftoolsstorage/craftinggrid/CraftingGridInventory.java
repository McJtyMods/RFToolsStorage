package mcjty.rftoolsstorage.craftinggrid;

import mcjty.lib.varia.ItemStackList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class CraftingGridInventory implements IInventory {

    public static int SLOT_GHOSTOUTPUT = 0;
    public static int SLOT_GHOSTINPUT = 1;

    public static int GRID_WIDTH = 66;
    public static int GRID_HEIGHT = 208;
    public static int GRID_XOFFSET = -GRID_WIDTH - 2 + 7;
    public static int GRID_YOFFSET = 127;

    private ItemStackList stacks = ItemStackList.create(10);

    public ItemStack getResult() {
        return stacks.get(SLOT_GHOSTOUTPUT);
    }

    public ItemStack[] getIngredients() {
        ItemStack[] ing = new ItemStack[9];
        for (int i = 0; i < ing.length; i++) {
            ing[i] = stacks.get(i + SLOT_GHOSTINPUT);
        }
        return ing;
    }

    @Override
    public int getSizeInventory() {
        return 10;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return stacks.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return index >= 0 && index < stacks.size()
                && !stacks.get(index).isEmpty()
                && count > 0
                ? stacks.get(index).split(count)
                : ItemStack.EMPTY;
//        return ItemStackHelper.getAndSplit(stacks, index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return index >= 0 && index < stacks.size()
                ? stacks.set(index, ItemStack.EMPTY)
                : ItemStack.EMPTY;
//        return ItemStackHelper.getAndRemove(stacks, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        stacks.set(index, stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
