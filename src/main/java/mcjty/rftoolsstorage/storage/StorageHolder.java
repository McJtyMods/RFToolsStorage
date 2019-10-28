package mcjty.rftoolsstorage.storage;

import mcjty.lib.worlddata.AbstractWorldData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StorageHolder extends AbstractWorldData<StorageHolder> implements IStorageListener {

    public static final String NAME = "RFToolsStorageHolder";

    private final Map<UUID, StorageEntry> storageEntryMap = new HashMap<>();

    private StorageHolder() {
        super(NAME);
    }

    public static StorageHolder get() {
        return getData(StorageHolder::new, NAME);
    }


    public StorageEntry getStorageEntry(UUID uuid) {
        if (!storageEntryMap.containsKey(uuid)) {
            StorageEntry entry = new StorageEntry(100, uuid, this); // @todo size
            storageEntryMap.put(uuid, entry);
            save();
        }
        return storageEntryMap.get(uuid);
    }

    @Override
    public void read(CompoundNBT nbt) {
        ListNBT storages = nbt.getList("Storages", Constants.NBT.TAG_COMPOUND);
        for (INBT storage : storages) {
            StorageEntry entry = new StorageEntry((CompoundNBT) storage, this);
            storageEntryMap.put(entry.getUuid(), entry);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        ListNBT storages = new ListNBT();
        for (Map.Entry<UUID, StorageEntry> entry : storageEntryMap.entrySet()) {
            storages.add(entry.getValue().write());
        }
        nbt.put("Storages", storages);
        return nbt;
    }

    @Override
    public void entryChanged(StorageEntry entry) {
        save();
    }
}
