package mcjty.rftoolsstorage.modules.modularstorage.blocks;

import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.storage.StorageEntry;
import mcjty.rftoolsstorage.storage.StorageHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

public class GlobalStorageItemWrapper implements IItemHandlerModifiable {

    private UUID uuid;
    private int version;
    private StorageEntry storage;
    private NonNullList<ItemStack> emptyHandler = NonNullList.withSize(400, ItemStack.EMPTY);   // @todo how to determine size?
    private final boolean remote;

    public GlobalStorageItemWrapper(UUID uuid, int version, boolean remote) {
        this.uuid = uuid;
        this.version = version;
        this.remote = remote;
    }

    public void setUuid(UUID uuid, int version) {
        if (Objects.equals(uuid, this.uuid) && version == this.version) {
            return;
        }
        this.uuid = uuid;
        this.version = version;
        storage = null;
    }

    private void createStorage() {
        if (storage == null && uuid != null) {
            if (remote) {
                storage = RFToolsStorage.setup.clientStorageHolder.getStorage(uuid, version);
            } else {
                storage = StorageHolder.get().getStorageEntry(uuid);
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
            StorageHolder.get().save();
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
        if (amount == 0) {
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
        return true;
    }
}
