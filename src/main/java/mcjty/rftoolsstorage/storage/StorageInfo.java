package mcjty.rftoolsstorage.storage;

import java.util.UUID;

public record StorageInfo(UUID uuid, int version, int size, String createdBy) {

    public static final StorageInfo EMPTY = new StorageInfo(null, -1, 0, null);

    public boolean isEmpty() {
        return uuid == null;
    }
}
