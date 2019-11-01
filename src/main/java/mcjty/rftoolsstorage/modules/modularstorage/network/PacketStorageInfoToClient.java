package mcjty.rftoolsstorage.modules.modularstorage.network;

import mcjty.lib.McJtyLib;
import mcjty.lib.network.NetworkTools;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketStorageInfoToClient {

    private BlockPos pos;
    private String viewMode;
    private String sortMode;
    private boolean groupMode;
    private String filter;


    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        NetworkTools.writeString(buf, viewMode);
        NetworkTools.writeString(buf, sortMode);
        buf.writeBoolean(groupMode);
        NetworkTools.writeString(buf, filter);
    }

    public PacketStorageInfoToClient() {
    }

    public PacketStorageInfoToClient(PacketBuffer buf) {
        pos = buf.readBlockPos();
        viewMode = NetworkTools.readString(buf);
        sortMode = NetworkTools.readString(buf);
        groupMode = buf.readBoolean();
        filter = NetworkTools.readString(buf);
    }

    public PacketStorageInfoToClient(BlockPos pos,
                                     String sortMode, String viewMode, boolean groupMode, String filter) {
        this.sortMode = sortMode;
        this.viewMode = viewMode;
        this.groupMode = groupMode;
        this.filter = filter;
        this.pos = pos;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = McJtyLib.proxy.getClientWorld().getTileEntity(pos);
            if (te instanceof ModularStorageTileEntity) {
                ModularStorageTileEntity storage = (ModularStorageTileEntity) te;
                storage.syncInventoryFromServer(sortMode, viewMode, groupMode, filter);
            }
        });
        ctx.setPacketHandled(true);
    }
}
