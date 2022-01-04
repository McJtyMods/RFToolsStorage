package mcjty.rftoolsstorage.modules.modularstorage.network;

import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageTileEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketStorageInfoToClient {

    private BlockPos pos;
    private String viewMode;
    private String sortMode;
    private boolean groupMode;
    private String filter;
    private boolean locked;

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(viewMode);
        buf.writeUtf(sortMode);
        buf.writeBoolean(groupMode);
        buf.writeUtf(filter);
        buf.writeBoolean(locked);
    }

    public PacketStorageInfoToClient() {
    }

    public PacketStorageInfoToClient(FriendlyByteBuf buf) {
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
            BlockEntity te = SafeClientTools.getClientWorld().getBlockEntity(pos);
            if (te instanceof ModularStorageTileEntity) {
                ModularStorageTileEntity storage = (ModularStorageTileEntity) te;
                storage.syncInventoryFromServer(sortMode, viewMode, groupMode, filter, locked);
            }
        });
        ctx.setPacketHandled(true);
    }
}
