package mcjty.rftoolsstorage.modules.scanner.network;

import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.DimensionId;
import mcjty.lib.varia.WorldTools;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketRequestItem {

    private DimensionId dimensionId;
    private BlockPos pos;
    private BlockPos inventoryPos;
    private ItemStack item;
    private int amount;
    private boolean craftable;

    public void toBytes(PacketBuffer buf) {
        dimensionId.toBytes(buf);
        buf.writeBlockPos(pos);
        buf.writeBlockPos(inventoryPos);
        buf.writeInt(amount);
        NetworkTools.writeItemStack(buf, item);
        buf.writeBoolean(craftable);
    }

    public PacketRequestItem() {
    }

    public PacketRequestItem(PacketBuffer buf) {
        dimensionId = DimensionId.fromPacket(buf);
        pos = buf.readBlockPos();
        inventoryPos = buf.readBlockPos();
        amount = buf.readInt();
        item = NetworkTools.readItemStack(buf);
        craftable = buf.readBoolean();
    }

    public PacketRequestItem(DimensionId dimensionId, BlockPos pos, BlockPos inventoryPos, ItemStack item, int amount, boolean craftable) {
        this.dimensionId = dimensionId;
        this.pos = pos;
        this.inventoryPos = inventoryPos;
        this.item = item;
        this.amount = amount;
        this.craftable = craftable;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            World world = WorldTools.getWorld(ctx.getSender().world, dimensionId);
            if (world == null) {
                return;
            }
            if (!WorldTools.isLoaded(world, pos)) {
                return;
            }
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof StorageScannerTileEntity) {
                StorageScannerTileEntity tileEntity = (StorageScannerTileEntity) te;
                if (craftable) {
                    tileEntity.requestCraft(inventoryPos, item, amount, ctx.getSender());
                } else {
                    tileEntity.requestStack(inventoryPos, item, amount, ctx.getSender());
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
