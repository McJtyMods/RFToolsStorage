package mcjty.rftoolsstorage.storage.network;

import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.storage.StorageEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketReturnStorageToClient {
    private final UUID uuid;
    private final StorageEntry entry;

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        CompoundTag nbt = entry.write();
        buf.writeNbt(nbt);
    }

    public PacketReturnStorageToClient(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        CompoundTag nbt = buf.readNbt();
        entry = new StorageEntry(nbt);
    }

    public PacketReturnStorageToClient(UUID uuid, StorageEntry entry) {
        this.uuid = uuid;
        this.entry = entry;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            // @todo 1.18, investigate why adding this breaks things
//            RFToolsStorage.setup.clientStorageHolder.registerStorage(uuid, entry);
        });
        ctx.setPacketHandled(true);
    }
}
