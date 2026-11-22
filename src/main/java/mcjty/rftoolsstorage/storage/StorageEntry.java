package mcjty.rftoolsstorage.storage;

import net.minecraft.core.UUIDUtil;
import mcjty.rftoolsstorage.craftinggrid.CraftingData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A list of items that can be coupled to a storage card
 */
public class StorageEntry {

    private record StoredStack(int slot, ItemStack stack) {

        private static final Codec<StoredStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("Slot").forGetter(StoredStack::slot),
                ItemStack.CODEC.fieldOf("stack").forGetter(StoredStack::stack)
        ).apply(instance, StoredStack::new));
    }

    public static final Codec<StorageEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("slots").forGetter(entry -> entry.stacks.size()),
            UUIDUtil.CODEC.fieldOf("UUID").forGetter(StorageEntry::getUuid),
            Codec.INT.fieldOf("version").forGetter(StorageEntry::getVersion),
            Codec.LONG.fieldOf("crTime").forGetter(StorageEntry::getCreationTime),
            Codec.LONG.fieldOf("upTime").forGetter(StorageEntry::getUpdateTime),
            Codec.STRING.fieldOf("createdBy").forGetter(StorageEntry::getCreatedBy),
            StoredStack.CODEC.listOf().fieldOf("Items").forGetter(StorageEntry::getStoredStacks)
    ).apply(instance, StorageEntry::new));

    private NonNullList<ItemStack> stacks;
    private final UUID uuid;
    private int version;
    private final long creationTime;
    private long updateTime;
    private String createdBy;

    private StorageEntry(int size, UUID uuid, int version, long creationTime, long updateTime,
                         String createdBy, List<StoredStack> storedStacks) {
        this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        this.uuid = uuid;
        this.version = version;
        this.creationTime = creationTime;
        this.updateTime = updateTime;
        this.createdBy = createdBy;
        for (StoredStack storedStack : storedStacks) {
            if (storedStack.slot() >= 0 && storedStack.slot() < size) {
                stacks.set(storedStack.slot(), storedStack.stack());
            }
        }
    }

    private List<StoredStack> getStoredStacks() {
        List<StoredStack> storedStacks = new ArrayList<>();
        for (int slot = 0; slot < stacks.size(); slot++) {
            ItemStack stack = stacks.get(slot);
            if (!stack.isEmpty()) {
                storedStacks.add(new StoredStack(slot, stack));
            }
        }
        return storedStacks;
    }

    public StorageEntry(CompoundTag nbt, HolderLookup.Provider provider) {
        int size = nbt.getIntOr("slots", 0);
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        ListTag tagList = nbt.getListOrEmpty("Items");
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag itemTags = tagList.getCompoundOrEmpty(i);
            int slot = itemTags.getIntOr("Slot", 0);

            if (slot >= 0 && slot < stacks.size()) {
                stacks.set(slot, CraftingData.readStack(provider, itemTags));
            }
        }

        uuid = nbt.read("UUID", UUIDUtil.CODEC).orElse(null);
        version = nbt.getIntOr("version", 0);
        creationTime = nbt.getLongOr("crTime", 0L);
        updateTime = nbt.getLongOr("upTime", 0L);
        createdBy = nbt.getStringOr("createdBy", "");
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
