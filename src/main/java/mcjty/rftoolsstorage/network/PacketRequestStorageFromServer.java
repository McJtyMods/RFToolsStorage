package mcjty.rftoolsstorage.network;

import mcjty.lib.network.IClientCommandHandler;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsstorage.storage.StorageEntry;
import mcjty.rftoolsstorage.storage.StorageHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.ItemStackHandler;

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
            RFToolsStorageMessages.INSTANCE.sendTo();

            TileEntity te = Minecraft.getInstance().world.getTileEntity(pos);
            if(!(te instanceof IClientCommandHandler)) {
                Logging.log("createInventoryReadyPacket: TileEntity is not a ClientCommandHandler!");
                return;
            }
            IClientCommandHandler clientCommandHandler = (IClientCommandHandler) te;
            if (!clientCommandHandler.receiveDataFromServer(command, result)) {
                Logging.log("Command " + command + " was not handled!");
            }
        });
        ctx.setPacketHandled(true);
    }
}
