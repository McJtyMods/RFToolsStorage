package mcjty.rftoolsstorage.storage;

import mcjty.lib.varia.LevelTools;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class GlobalStorageItemWrapper implements IItemHandlerModifiable, ResourceHandler<ItemResource> {

    @Nonnull private StorageInfo info;
    private StorageEntry storage;
    private NonNullList<ItemStack> emptyHandler = NonNullList.withSize(10, ItemStack.EMPTY);
    private final boolean remote;
    private IStorageListener listener;

    private final SnapshotJournal<List<ItemStack>> transfers = new SnapshotJournal<>() {

        @Override
        protected List<ItemStack> createSnapshot() {
            return getStacks().stream().map(ItemStack::copy).toList();
        }

        @Override
        protected void revertToSnapshot(List<ItemStack> snapshot) {
            for (int slot = 0; slot < snapshot.size(); slot++) {
                getStacks().set(slot, snapshot.get(slot));
            }
        }

        @Override
        protected void onRootCommit(List<ItemStack> before) {
            for (int slot = 0; slot < before.size(); slot++) {
                if (!ItemStack.matches(before.get(slot), getStackInSlot(slot))) {
                    onContentsChanged(slot);
                }
            }
        }
    };

    public GlobalStorageItemWrapper(@Nonnull StorageInfo info, boolean remote) {
        this.info = info;
        this.remote = remote;
    }

    public void setInfo(@Nonnull StorageInfo info) {
        if (Objects.equals(info, this.info)) {
            return;
        }
        this.info = info;
        if (info.size() != emptyHandler.size()) {
            if (info.size() > 0) {
                emptyHandler = NonNullList.withSize(info.size(), ItemStack.EMPTY);
            }
        }
        storage = null;
    }

    public void setListener(IStorageListener listener) {
        this.listener = listener;
    }

    private void createStorage() {
        if (storage == null && !info.isEmpty()) {
            if (!remote) {
                storage = StorageHolder.get(LevelTools.getOverworld()).getOrCreateStorageEntry(info.uuid(), info.size(), info.createdBy());
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

    public StorageEntry getStorage() {
        createStorage();
        return storage;
    }

    @Override
    public int size() {
        return getSlots();
    }

    @Override
    public ItemResource getResource(int slot) {
        return ItemResource.of(getStackInSlot(slot));
    }

    @Override
    public long getAmountAsLong(int slot) {
        return getStackInSlot(slot).getCount();
    }

    @Override
    public long getCapacityAsLong(int slot, ItemResource resource) {
        return resource.isEmpty() ? getSlotLimit(slot) : getStackLimit(slot, resource.toStack());
    }

    @Override
    public boolean isValid(int slot, ItemResource resource) {
        return !resource.isEmpty() && isItemValid(slot, resource.toStack());
    }

    @Override
    public int insert(int slot, ItemResource resource, int amount, TransactionContext transaction) {
        if (amount <= 0 || !isValid(slot, resource)) {
            return 0;
        }
        ItemStack candidate = resource.toStack(amount);
        int moved = amount - insertItem(slot, candidate, true).getCount();
        if (moved > 0) {
            transfers.updateSnapshots(transaction);
            getStacks().set(slot, resource.toStack(getStackInSlot(slot).getCount() + moved));
        }
        return moved;
    }

    @Override
    public int extract(int slot, ItemResource resource, int amount, TransactionContext transaction) {
        ItemStack current = getStackInSlot(slot);
        if (amount <= 0 || resource.isEmpty() || !resource.matches(current)) {
            return 0;
        }
        int moved = extractItem(slot, amount, true).getCount();
        if (moved > 0) {
            transfers.updateSnapshots(transaction);
            getStacks().set(slot, current.copyWithCount(current.getCount() - moved));
        }
        return moved;
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
            StorageHolder.get(LevelTools.getOverworld()).setDirty();
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
            if (!ItemStack.isSameItemSameComponents(stack, existing)) {
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
                stacks.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack);
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged(slot);
        }

        return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
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
                stacks.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
                onContentsChanged(slot);
            }

            return existing.copyWithCount(toExtract);
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
        if (slot >= info.size()) {
            return false;
        }
        return true;
    }


}
