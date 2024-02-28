package mcjty.rftoolsstorage.craftinggrid;

import mcjty.lib.varia.ItemStackList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public class CraftingGridInventory implements IItemHandlerModifiable {

    public static final int SLOT_GHOSTOUTPUT = 0;
    public static final int SLOT_GHOSTINPUT = 1;

    public static final int GRID_WIDTH = 66;
    public static final int GRID_HEIGHT = 208;
    public static final int GRID_XOFFSET = -GRID_WIDTH - 2 + 7;
    public static final int GRID_YOFFSET = 127;

    private final ItemStackList stacks = ItemStackList.create(10);

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
    public int getSlots() {
        return 10;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        // @todo 1.14
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        // @todo 1.14
        return ItemStack.EMPTY;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        stacks.set(slot, stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 0;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return false;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int index) {
        return stacks.get(index);
    }
}
