package mcjty.rftoolsstorage.storage.network;

import mcjty.rftoolsstorage.setup.RFToolsStorageMessages;
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
        buf.writeUUID(uuid);
    }

    public PacketRequestStorageFromServer(PacketBuffer buf) {
        this.uuid = buf.readUUID();
    }

    public PacketRequestStorageFromServer(UUID uuid) {
        this.uuid = uuid;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            StorageEntry entry = StorageHolder.get(ctx.getSender().getLevel()).getStorageEntry(uuid);
            if (entry != null) {
                RFToolsStorageMessages.INSTANCE.sendTo(new PacketReturnStorageToClient(uuid, entry), ctx.getSender().connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
        });
        ctx.setPacketHandled(true);
    }
}
