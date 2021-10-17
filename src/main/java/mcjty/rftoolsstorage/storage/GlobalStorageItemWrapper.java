package mcjty.rftoolsstorage.storage;

import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsstorage.RFToolsStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.Objects;

public class GlobalStorageItemWrapper implements IItemHandlerModifiable {

    @Nonnull private StorageInfo info;
    private StorageEntry storage;
    private NonNullList<ItemStack> emptyHandler = NonNullList.withSize(10, ItemStack.EMPTY);
    private final boolean remote;
    private IStorageListener listener;

    public GlobalStorageItemWrapper(@Nonnull StorageInfo info, boolean remote) {
        this.info = info;
        this.remote = remote;
    }

    public void setInfo(@Nonnull StorageInfo info) {
        if (Objects.equals(info, this.info)) {
            return;
        }
        this.info = info;
        if (info.getSize() != emptyHandler.size()) {
            emptyHandler = NonNullList.withSize(info.getSize(), ItemStack.EMPTY);
        }
        storage = null;
    }

    public void setListener(IStorageListener listener) {
        this.listener = listener;
    }

    private void createStorage() {
        if (storage == null && info.getUuid() != null) {
            if (remote) {
                storage = RFToolsStorage.setup.clientStorageHolder.getStorage(info.getUuid(), info.getVersion());
            } else {
                storage = StorageHolder.get(LevelTools.getOverworld()).getOrCreateStorageEntry(info.getUuid(), info.getSize(), info.getCreatedBy());
            }
        }
    }

    @Nonnull
    private NonNullList<ItemStack> getStacks() {
        createStorage();
        if (storage == null) {
            return emptyHandler;
        } else {
            return storage.getStacks();
        }
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (info.isEmpty()) {
            return;
        }
//        validateSlotIndex(slot);
        NonNullList<ItemStack> stacks = getStacks();
        if (slot >= stacks.size()) {
            return;
        }
        stacks.set(slot, stack);
        onContentsChanged(slot);
    }

    private void onContentsChanged(int slot) {
        if (!remote) {
            if (storage != null) {
                storage.updateVersion();
                if (listener != null) {
                    listener.onContentsChanged(storage.getVersion(), slot);
                }
            }
            StorageHolder.get(LevelTools.getOverworld()).save();
        }
    }

    @Override
    public int getSlots() {
        return getStacks().size();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
//        validateSlotIndex(slot);
        NonNullList<ItemStack> stacks = getStacks();
        if (slot >= stacks.size()) {
            return ItemStack.EMPTY;
        }
        return stacks.get(slot);
    }

    private int getStackLimit(int slot, @Nonnull ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }


    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (info.isEmpty()) {
            return stack;
        }

        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (!isItemValid(slot, stack)) {
            return stack;
        }

//        validateSlotIndex(slot);
        NonNullList<ItemStack> stacks = getStacks();
        if (slot >= stacks.size()) {
            return stack;
        }

        ItemStack existing = stacks.get(slot);

        int limit = getStackLimit(slot, stack);

        if (!existing.isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) {
                return stack;
            }

            limit -= existing.getCount();
        }

        if (limit <= 0) {
            return stack;
        }

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                stacks.set(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged(slot);
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0 || info.isEmpty()) {
            return ItemStack.EMPTY;
        }

//        validateSlotIndex(slot);
        NonNullList<ItemStack> stacks = getStacks();
        if (slot >= stacks.size()) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = stacks.get(slot);

        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
            }
            return existing;
        } else {
            if (!simulate) {
                stacks.set(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
                onContentsChanged(slot);
            }

            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (info.isEmpty()) {
            return false;
        }
        if (slot >= info.getSize()) {
            return false;
        }
        return true;
    }


}
