package mcjty.rftoolsstorage.modules.scanner.network;


import mcjty.rftoolsstorage.modules.scanner.client.GuiStorageScanner;
import net.minecraft.block.Block;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketReturnInventoryInfo {

    private List<InventoryInfo> inventories;

    public List<InventoryInfo> getInventories() {
        return inventories;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(inventories.size());
        for (InventoryInfo info : inventories) {
            buf.writeBlockPos(info.getPos());
            buf.writeString(info.getName());
            buf.writeBoolean(info.isRoutable());
            if (info.getBlock() == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                String id = info.getBlock().getRegistryName().toString();
                buf.writeString(id);
            }
        }
    }

    public PacketReturnInventoryInfo() {
    }

    public PacketReturnInventoryInfo(PacketBuffer buf) {
        int size = buf.readInt();
        inventories = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            BlockPos pos = buf.readBlockPos();
            String name = buf.readString();
            boolean routable = buf.readBoolean();
            Block block = null;
            if (buf.readBoolean()) {
                block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(buf.readString()));
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

    public static class InventoryInfo {
        private final BlockPos pos;
        private final String name;
        private final boolean routable;
        private final Block block;

        public InventoryInfo(BlockPos pos, String name, boolean routable, Block block) {
            this.pos = pos;
            this.name = name;
            this.routable = routable;
            this.block = block;
        }

        public BlockPos getPos() {
            return pos;
        }

        public String getName() {
            return name;
        }

        public boolean isRoutable() {
            return routable;
        }

        public Block getBlock() {
            return block;
        }
    }
}