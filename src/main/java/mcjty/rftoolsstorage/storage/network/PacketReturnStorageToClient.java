package mcjty.rftoolsstorage.storage.network;

import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.storage.StorageEntry;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketReturnStorageToClient {
    private UUID uuid;
    private StorageEntry entry;

    public void toBytes(PacketBuffer buf) {
        buf.writeUniqueId(uuid);
        CompoundNBT nbt = entry.write();
        buf.writeCompoundTag(nbt);
    }

    public PacketReturnStorageToClient(PacketBuffer buf) {
        this.uuid = buf.readUniqueId();
        CompoundNBT nbt = buf.readCompoundTag();
        entry = new StorageEntry(nbt);
    }

    public PacketReturnStorageToClient(UUID uuid, StorageEntry entry) {
        this.uuid = uuid;
        this.entry = entry;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            RFToolsStorage.setup.clientStorageHolder.registerStorage(uuid, entry);
        });
        ctx.setPacketHandled(true);
    }
}
