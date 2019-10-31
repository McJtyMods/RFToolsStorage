package mcjty.rftoolsstorage.network;

import mcjty.rftoolsstorage.storage.StorageEntry;
import mcjty.rftoolsstorage.storage.StorageHolder;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketRequestStorageFromServer {
    private UUID uuid;

    public void toBytes(PacketBuffer buf) {
        buf.writeUniqueId(uuid);
    }

    public PacketRequestStorageFromServer(PacketBuffer buf) {
        this.uuid = buf.readUniqueId();
    }

    public PacketRequestStorageFromServer(UUID uuid) {
        this.uuid = uuid;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            StorageEntry entry = StorageHolder.get().getStorageEntry(uuid);
            if (entry != null) {
                RFToolsStorageMessages.INSTANCE.sendTo(new PacketReturnStorageToClient(uuid, entry), ctx.getSender().connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
            }
        });
        ctx.setPacketHandled(true);
    }
}
