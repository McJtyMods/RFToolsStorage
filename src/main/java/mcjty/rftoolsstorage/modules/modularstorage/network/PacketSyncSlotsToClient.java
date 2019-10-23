package mcjty.rftoolsstorage.modules.modularstorage.network;

import mcjty.lib.McJtyLib;
import mcjty.lib.network.NetworkTools;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageTileEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketSyncSlotsToClient {

    private BlockPos pos;
    private String viewMode;
    private String sortMode;
    private boolean groupMode;
    private String filter;
    private List<Pair<Integer, ItemStack>> items;


    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        NetworkTools.writeString(buf, viewMode);
        NetworkTools.writeString(buf, sortMode);
        buf.writeBoolean(groupMode);
        NetworkTools.writeString(buf, filter);
        buf.writeInt(items.size());
        for (Pair<Integer, ItemStack> pair : items) {
            if (pair.getRight().isEmpty()) {
                buf.writeInt(-pair.getLeft()-1);  // Negative index to indicate an empty stack
            } else {
                buf.writeInt(pair.getLeft());
                buf.writeItemStack(pair.getRight());
            }
        }
    }

    public PacketSyncSlotsToClient() {
    }

    public PacketSyncSlotsToClient(PacketBuffer buf) {
        pos = buf.readBlockPos();
        viewMode = NetworkTools.readString(buf);
        sortMode = NetworkTools.readString(buf);
        groupMode = buf.readBoolean();
        filter = NetworkTools.readString(buf);
        int s = buf.readInt();
        items = new ArrayList<>(s);
        for (int i = 0 ; i < s ; i++) {
            int slotIdx = buf.readInt();
            if (slotIdx < 0) {
                items.add(Pair.of(-slotIdx-1, ItemStack.EMPTY));
            } else {
                ItemStack stack = buf.readItemStack();
                items.add(Pair.of(slotIdx, stack));
            }
        }
    }

    public PacketSyncSlotsToClient(BlockPos pos,
                                   String sortMode, String viewMode, boolean groupMode, String filter,
                                   List<Pair<Integer, ItemStack>> items) {
        this.sortMode = sortMode;
        this.viewMode = viewMode;
        this.groupMode = groupMode;
        this.filter = filter;
        this.pos = pos;
        this.items = items;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = McJtyLib.proxy.getClientWorld().getTileEntity(pos);
            if (te instanceof ModularStorageTileEntity) {
                ModularStorageTileEntity storage = (ModularStorageTileEntity) te;
                storage.syncInventoryFromServer(sortMode, viewMode, groupMode, filter);
                Container container = McJtyLib.proxy.getClientPlayer().openContainer;
                if (container instanceof ModularStorageContainer) {
                    for (Pair<Integer, ItemStack> pair : items) {
                        container.putStackInSlot(pair.getLeft(), pair.getRight());
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
