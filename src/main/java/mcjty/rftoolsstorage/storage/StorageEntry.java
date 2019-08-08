package mcjty.rftoolsstorage.storage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.ItemStackHandler;

import java.util.UUID;

/**
 * A list of items that can be coupled to a storage card
 */
public class StorageEntry {

    private final ItemStackHandler items;
    private final UUID uuid;

    public StorageEntry(CompoundNBT nbt, IStorageListener listener) {
        int slots = nbt.getInt("slots");
        items = createHandler(slots, listener);
        items.deserializeNBT(nbt.getCompound("Items"));
        uuid = nbt.getUniqueId("UUID");
    }

    public StorageEntry(int size, UUID uuid, IStorageListener listener) {
        items = createHandler(size, listener);
        this.uuid = uuid;
    }

    private ItemStackHandler createHandler(int size, IStorageListener listener) {
        return new ItemStackHandler(size) {
            @Override
            protected void onContentsChanged(int slot) {
                listener.entryChanged(StorageEntry.this);
                super.onContentsChanged(slot);
            }
        };
    }

    public ItemStackHandler getHandler() {
        return items;
    }

    public UUID getUuid() {
        return uuid;
    }

    public CompoundNBT write() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("slots", items.getSlots());
        nbt.put("Items", items.serializeNBT());
        nbt.putUniqueId("UUID", uuid);
        return nbt;
    }
}
