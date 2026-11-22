package mcjty.rftoolsstorage.storage;

import mcjty.lib.container.IndexedStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import java.util.List;
import mcjty.lib.varia.LevelTools;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import mcjty.lib.container.IItemHandlerModifiable;
import javax.annotation.Nonnull;
import java.util.Objects;

public class GlobalStorageItemWrapper implements IItemHandlerModifiable, IndexedStorage<ItemVariant> {

    private final SnapshotParticipant<List<ItemStack>> transfers = new SnapshotParticipant<>() {

        private List<ItemStack> before;

        @Override
        protected List<ItemStack> createSnapshot() {
            return getStacks().stream().map(ItemStack::copy).toList();
        }

        @Override
        protected void readSnapshot(List<ItemStack> snapshot) {
            for (int slot = 0; slot < snapshot.size(); slot++) {
                getStacks().set(slot, snapshot.get(slot));
            }
        }

        @Override
        protected void releaseSnapshot(List<ItemStack> snapshot) {
            before = snapshot;
        }

        @Override
        protected void onFinalCommit() {
            for (int slot = 0; slot < before.size(); slot++) {
                if (!ItemStack.matches(before.get(slot), getStackInSlot(slot))) {
                    onContentsChanged(slot);
                }
            }
            before = null;
        }
    };

    @Override
    public int size() {
        return info.isEmpty() ? 0 : getSlots();
    }

    @Override
    public ItemVariant getResource(int index) {
        return ItemVariant.of(getStackInSlot(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return getStackInSlot(index).getCount();
    }

    @Override
    public long getCapacityAsLong(int index, ItemVariant resource) {
        return resource.isBlank() ? getSlotLimit(index) : getStackLimit(index, resource.toStack());
    }

    @Override
    public boolean isValid(int index, ItemVariant resource) {
        return !resource.isBlank() && isItemValid(index, resource.toStack());
    }

    @Override
    public long insert(int index, ItemVariant resource, long amount, TransactionContext transaction) {
        if (amount <= 0 || !isValid(index, resource)) {
            return 0;
        }
        ItemStack candidate = resource.toStack((int) Math.min(amount, Integer.MAX_VALUE));
        int moved = candidate.getCount() - insertItem(index, candidate, true).getCount();
        if (moved > 0) {
            transfers.updateSnapshots(transaction);
            getStacks().set(index, resource.toStack(getStackInSlot(index).getCount() + moved));
        }
        return moved;
    }

    @Override
    public long extract(int index, ItemVariant resource, long amount, TransactionContext transaction) {
        ItemStack current = getStackInSlot(index);
        if (amount <= 0 || resource.isBlank() || !resource.matches(current)) {
            return 0;
        }
        int moved = extractItem(index, (int) Math.min(amount, Integer.MAX_VALUE), true).getCount();
        if (moved > 0) {
            transfers.updateSnapshots(transaction);
            getStacks().set(index, current.copyWithCount(current.getCount() - moved));
        }
        return moved;
    }


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
