package mcjty.rftoolsstorage.storage;

import mcjty.rftoolsstorage.setup.RFToolsStorageMessages;
import mcjty.rftoolsstorage.storage.network.PacketRequestStorageFromServer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Client version of the storage holder
 */
public class ClientStorageHolder {

    private final Map<UUID, StorageEntry> storageEntryMap = new HashMap<>();

    // Tries to fetch the items for this storage entry. This can return null
    // if the data is not present on the client (in that case a new version is
    // requested from the server). If the data is present but outdated then
    // a new version is requested too but the old data is returned.
    @Nullable
    public StorageEntry getStorage(UUID uuid, int version) {
        if (!storageEntryMap.containsKey(uuid)) {
            requestData(uuid);
            return null;
        }
        StorageEntry entry = storageEntryMap.get(uuid);
        if (entry.getVersion() != version) {
            requestData(uuid);
        }
        return entry;
    }

    private void requestData(UUID uuid) {
        RFToolsStorageMessages.INSTANCE.sendToServer(new PacketRequestStorageFromServer(uuid));
    }

    public void registerStorage(UUID uuid, StorageEntry entry) {
        storageEntryMap.put(uuid, entry);
    }

}
