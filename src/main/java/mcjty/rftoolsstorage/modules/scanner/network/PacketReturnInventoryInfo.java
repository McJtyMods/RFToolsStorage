package mcjty.rftoolsstorage.modules.scanner.network;


import mcjty.rftoolsstorage.modules.scanner.client.GuiStorageScanner;
import net.minecraft.world.level.block.Block;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketReturnInventoryInfo {

    private final List<InventoryInfo> inventories;

    public List<InventoryInfo> getInventories() {
        return inventories;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(inventories.size());
        for (InventoryInfo info : inventories) {
            buf.writeBlockPos(info.pos());
            buf.writeUtf(info.name());
            buf.writeBoolean(info.routable());
            if (info.block() == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                String id = info.block().getRegistryName().toString();
                buf.writeUtf(id);
            }
        }
    }

    public PacketReturnInventoryInfo(FriendlyByteBuf buf) {
        int size = buf.readInt();
        inventories = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            BlockPos pos = buf.readBlockPos();
            String name = buf.readUtf(32767);
            boolean routable = buf.readBoolean();
            Block block = null;
            if (buf.readBoolean()) {
                block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(buf.readUtf(32767)));
            }
            inventories.add(new InventoryInfo(pos, name, routable, block));
        }
    }

    public PacketReturnInventoryInfo(List<InventoryInfo> inventories) {
        this.inventories = inventories;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            GuiStorageScanner.fromServer_inventories = getInventories();
        });
        ctx.setPacketHandled(true);
    }

    public record InventoryInfo(BlockPos pos, String name, boolean routable, Block block) {
    }
}