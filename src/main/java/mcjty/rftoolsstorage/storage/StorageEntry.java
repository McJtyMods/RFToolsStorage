package mcjty.rftoolsstorage.storage;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * A list of items that can be coupled to a storage card
 */
public class StorageEntry {

    private NonNullList<ItemStack> stacks;
    private final UUID uuid;
    private int version;
    private long creationTime;
    private long updateTime;
    private String createdBy;

    public StorageEntry(CompoundTag nbt) {
        int size = nbt.getInt("slots");
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");

            if (slot >= 0 && slot < stacks.size()) {
                stacks.set(slot, ItemStack.of(itemTags));
            }
        }

        uuid = nbt.hasUUID("UUID") ? nbt.getUUID("UUID") : null;
        version = nbt.getInt("version");
        creationTime = nbt.getLong("crTime");
        updateTime = nbt.getLong("upTime");
        createdBy = nbt.getString("createdBy");
    }

    public StorageEntry(int size, UUID uuid, String createdBy) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        this.uuid = uuid;
        this.version = 1;
        this.creationTime = this.updateTime = System.currentTimeMillis();
        this.createdBy = createdBy == null ? "" : createdBy;
    }

    public int getVersion() {
        return version;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void updateVersion() {
        version++;
        updateTime = System.currentTimeMillis();
    }

    public UUID getUuid() {
        return uuid;
    }

    public NonNullList<ItemStack> getStacks() {
        return stacks;
    }

    public CompoundTag write() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("slots", stacks.size());
        nbt.putInt("version", version);
        nbt.putLong("crTime", creationTime);
        nbt.putLong("upTime", updateTime);
        nbt.putString("createdBy", createdBy);

        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                stacks.get(i).save(itemTag);
                nbtTagList.add(itemTag);
            }
        }
        nbt.put("Items", nbtTagList);
        nbt.putUUID("UUID", uuid);
        return nbt;
    }

    // Note: when resizing to a smaller size items might be lost
    public void resize(int size, String createdBy) {
        List<ItemStack> oldList = stacks;
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int i = 0 ; i < Math.min(oldList.size(), size) ; i++) {
            stacks.set(i, oldList.get(i));
        }
        updateTime = System.currentTimeMillis();
        if (createdBy != null && !createdBy.isEmpty()) {
            this.createdBy = createdBy;
        }
    }
}
