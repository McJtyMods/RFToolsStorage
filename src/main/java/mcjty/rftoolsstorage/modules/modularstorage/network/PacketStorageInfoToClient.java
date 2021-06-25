package mcjty.rftoolsstorage.modules.modularstorage.network;

import mcjty.lib.McJtyLib;
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
    private boolean locked;

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(viewMode);
        buf.writeUtf(sortMode);
        buf.writeBoolean(groupMode);
        buf.writeUtf(filter);
        buf.writeBoolean(locked);
    }

    public PacketStorageInfoToClient() {
    }

    public PacketStorageInfoToClient(PacketBuffer buf) {
        pos = buf.readBlockPos();
        viewMode = buf.readUtf(32767);
        sortMode = buf.readUtf(32767);
        groupMode = buf.readBoolean();
        filter = buf.readUtf(32767);
        locked = buf.readBoolean();
    }

    public PacketStorageInfoToClient(BlockPos pos,
                                     String sortMode, String viewMode, boolean groupMode, String filter, boolean locked) {
        this.sortMode = sortMode;
        this.viewMode = viewMode;
        this.groupMode = groupMode;
        this.filter = filter;
        this.pos = pos;
        this.locked = locked;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = McJtyLib.proxy.getClientWorld().getBlockEntity(pos);
            if (te instanceof ModularStorageTileEntity) {
                ModularStorageTileEntity storage = (ModularStorageTileEntity) te;
                storage.syncInventoryFromServer(sortMode, viewMode, groupMode, filter, locked);
            }
        });
        ctx.setPacketHandled(true);
    }
}
