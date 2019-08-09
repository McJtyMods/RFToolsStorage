package mcjty.rftoolsstorage.storage;

import net.minecraftforge.items.ItemStackHandler;

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
    public ItemStackHandler getStorage(UUID uuid, int version) {
        if (!storageEntryMap.containsKey(uuid)) {
            requestData(uuid);
            return null;
        }
        StorageEntry entry = storageEntryMap.get(uuid);
        if (entry.getVersion() != version) {
            requestData(uuid);
        }
        return entry.getHandler();
    }

    private void requestData(UUID uuid) {

    }

}
