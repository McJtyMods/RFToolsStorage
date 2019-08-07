package mcjty.rftoolsstorage.items;

import mcjty.lib.varia.ItemStackList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StorageModuleItemWrapper implements IItemHandler, ICapabilityProvider {

    private final LazyOptional<IItemHandler> holder = LazyOptional.of(() -> this);

    private ItemStackList itemStacks = ItemStackList.create(100);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return holder.cast();
    }

    @Override
    public int getSlots() {
        return itemStacks.size();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return itemStacks.get(slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack stackInSlot = itemStacks.get(slot);

        int m;
        if (!stackInSlot.isEmpty()) {
            if (stackInSlot.getCount() >= Math.min(stackInSlot.getMaxStackSize(), getSlotLimit(slot))) {
                return stack;
            }

            if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot)) {
                return stack;
            }

            if (!isItemValid(slot, stack)) {
                return stack;
            }

            m = Math.min(stack.getMaxStackSize(), getSlotLimit(slot)) - stackInSlot.getCount();

            if (stack.getCount() <= m) {
                if (!simulate) {
                    ItemStack copy = stack.copy();
                    copy.grow(stackInSlot.getCount());
                    itemStacks.set(slot, copy);
                }

                return ItemStack.EMPTY;
            } else {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate) {
                    ItemStack copy = stack.split(m);
                    copy.grow(stackInSlot.getCount());
                    itemStacks.set(slot, copy);
                    return stack;
                } else {
                    stack.shrink(m);
                    return stack;
                }
            }
        } else {
            if (!isItemValid(slot, stack)) {
                return stack;
            }

            m = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
            if (m < stack.getCount()) {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate) {
                    itemStacks.set(slot, stack.split(m));
                    return stack;
                } else {
                    stack.shrink(m);
                    return stack;
                }
            } else {
                if (!simulate) {
                    itemStacks.set(slot, stack);
                }
                return ItemStack.EMPTY;
            }
        }

    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = itemStacks.get(slot);

        if (stackInSlot.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (simulate) {
            if (stackInSlot.getCount() < amount) {
                return stackInSlot.copy();
            } else {
                ItemStack copy = stackInSlot.copy();
                copy.setCount(amount);
                return copy;
            }
        } else {
            int m = Math.min(stackInSlot.getCount(), amount);
            return decrStackSize(stackInSlot, slot, m);
        }
    }

    public ItemStack decrStackSize(ItemStack stackInSlot, int index, int amount) {
        if (!stackInSlot.isEmpty()) {
            if (stackInSlot.getCount() <= amount) {
                ItemStack old = stackInSlot;
                itemStacks.set(index, ItemStack.EMPTY);
                return old;
            }
            ItemStack its = stackInSlot.split(amount);
            if (stackInSlot.isEmpty()) {
                itemStacks.set(index, ItemStack.EMPTY);
            }
            return its;
        }
        return ItemStack.EMPTY;
    }


    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return true;
    }
}
