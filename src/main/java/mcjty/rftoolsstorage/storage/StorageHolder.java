package mcjty.rftoolsstorage.storage;

import com.mojang.serialization.Codec;
import mcjty.rftoolsstorage.RFToolsStorage;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelResource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StorageHolder extends SavedData {

    private static final String LEGACY_FILE = "RFToolsStorageHolder.dat";
    private static final Identifier ID = Identifier.fromNamespaceAndPath(RFToolsStorage.MODID, "storage_holder");
    private static final Codec<StorageHolder> CODEC = StorageEntry.CODEC.listOf().fieldOf("Storages").codec()
            .xmap(StorageHolder::new, holder -> new ArrayList<>(holder.storageEntryMap.values()));
    private static final SavedDataType<StorageHolder> TYPE = new SavedDataType<>(ID, StorageHolder::new, CODEC);

    private final Map<UUID, StorageEntry> storageEntryMap = new HashMap<>();

    private StorageHolder() {
    }

    private StorageHolder(List<StorageEntry> entries) {
        for (StorageEntry entry : entries) {
            storageEntryMap.put(entry.getUuid(), entry);
        }
    }

    public static StorageHolder get(Level world) {
        if (!(world instanceof ServerLevel level)) {
            throw new IllegalStateException("Storage cards can only access saved contents on the server");
        }
        var server = level.getServer();
        var data = server.getDataStorage();
        StorageHolder holder = data.get(TYPE);
        if (holder != null) {
            return holder;
        }
        var dataFolder = server.getWorldPath(LevelResource.DATA);
        if (Files.exists(ID.withSuffix(".dat").resolveAgainst(dataFolder))) {
            throw new IllegalStateException("Could not load RFTools Storage saved data");
        }
        var legacyFile = dataFolder.resolve(LEGACY_FILE);
        if (Files.isRegularFile(legacyFile)) {
            // Minecraft's old saved-data format used an unnamespaced filename in the world data folder.
            try {
                var rawTag = data.readTagFromDisk(legacyFile, null, SharedConstants.getCurrentVersion().dataVersion().version());
                var tag = rawTag.getCompound("data").orElseThrow(() -> new IOException("Missing saved data"));
                var entries = new ArrayList<StorageEntry>();
                for (var entry : tag.getListOrEmpty("Storages")) {
                    if (entry instanceof CompoundTag compound) {
                        var storage = new StorageEntry(compound, level.registryAccess());
                        if (storage.getUuid() != null) {
                            entries.add(storage);
                        }
                    }
                }
                holder = new StorageHolder(entries);
                data.set(TYPE, holder);
                return holder;
            } catch (IOException e) {
                throw new UncheckedIOException("Could not import legacy RFTools Storage saved data", e);
            }
        }
        return data.computeIfAbsent(TYPE);
    }

    public StorageEntry getOrCreateStorageEntry(UUID uuid, int size, String createdBy) {
        StorageEntry entry = storageEntryMap.get(uuid);
        if (entry == null) {
            entry = new StorageEntry(size, uuid, createdBy);
            storageEntryMap.put(uuid, entry);
            setDirty();
        } else if (size != entry.getStacks().size()) {
            entry.resize(size, createdBy);
            setDirty();
        }
        return entry;
    }

    public StorageEntry getStorageEntry(UUID uuid) {
        return storageEntryMap.get(uuid);
    }

    public Collection<StorageEntry> getStorages() {
        return storageEntryMap.values();
    }
}
