package mcjty.rftoolsstorage.modules.scanner.network;

import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketRequestItem {

    private final ResourceKey<Level> dimensionId;
    private final BlockPos pos;
    private final BlockPos inventoryPos;
    private final ItemStack item;
    private final int amount;
    private final boolean craftable;

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(dimensionId.location());
        buf.writeBlockPos(pos);
        buf.writeBlockPos(inventoryPos);
        buf.writeInt(amount);
        NetworkTools.writeItemStack(buf, item);
        buf.writeBoolean(craftable);
    }

    public PacketRequestItem(FriendlyByteBuf buf) {
        dimensionId = LevelTools.getId(buf.readResourceLocation());
        pos = buf.readBlockPos();
        inventoryPos = buf.readBlockPos();
        amount = buf.readInt();
        item = NetworkTools.readItemStack(buf);
        craftable = buf.readBoolean();
    }

    public PacketRequestItem(ResourceKey<Level>
                                     dimensionId, BlockPos pos, BlockPos inventoryPos, ItemStack item, int amount, boolean craftable) {
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
            Level world = LevelTools.getLevel(ctx.getSender().level, dimensionId);
            if (world == null) {
                return;
            }
            if (!LevelTools.isLoaded(world, pos)) {
                return;
            }
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof StorageScannerTileEntity scanner) {
                if (craftable) {
                    scanner.requestCraft(inventoryPos, item, amount, ctx.getSender());
                } else {
                    scanner.requestStack(inventoryPos, item, amount, ctx.getSender());
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
