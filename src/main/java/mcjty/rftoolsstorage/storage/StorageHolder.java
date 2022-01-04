package mcjty.rftoolsstorage.storage;

import mcjty.lib.worlddata.AbstractWorldData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StorageHolder extends AbstractWorldData<StorageHolder> {

    public static final String NAME = "RFToolsStorageHolder";

    private final Map<UUID, StorageEntry> storageEntryMap = new HashMap<>();

    private StorageHolder() {
    }

    private StorageHolder(CompoundTag tag) {
        ListTag storages = tag.getList("Storages", Tag.TAG_COMPOUND);
        for (Tag storage : storages) {
            StorageEntry entry = new StorageEntry((CompoundTag) storage);
            storageEntryMap.put(entry.getUuid(), entry);
        }
    }

    public static StorageHolder get(Level world) {
        return getData(world, StorageHolder::new, StorageHolder::new, NAME);
    }


    public StorageEntry getOrCreateStorageEntry(UUID uuid, int size, String createdBy) {
        if (!storageEntryMap.containsKey(uuid)) {
            StorageEntry entry = new StorageEntry(size, uuid, createdBy);
            storageEntryMap.put(uuid, entry);
            save();
        } else {
            // Check if the size still matches
            StorageEntry entry = storageEntryMap.get(uuid);
            if (size != entry.getStacks().size()) {
                entry.resize(size, createdBy);
            }
        }
        return storageEntryMap.get(uuid);
    }

    public StorageEntry getStorageEntry(UUID uuid) {
        return storageEntryMap.get(uuid);
    }

    public Collection<StorageEntry> getStorages() {
        return storageEntryMap.values();
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag nbt) {
        ListTag storages = new ListTag();
        for (Map.Entry<UUID, StorageEntry> entry : storageEntryMap.entrySet()) {
            storages.add(entry.getValue().write());
        }
        nbt.put("Storages", storages);
        return nbt;
    }
}
