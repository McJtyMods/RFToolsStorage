package mcjty.rftoolsstorage.modules.scanner.network;


import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.scanner.client.GuiStorageScanner;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public record PacketReturnInventoryInfo(List<InventoryInfo> inventories) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsStorage.MODID, "return_inventory_info");

    public List<InventoryInfo> getInventories() {
        return inventories;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(inventories.size());
        for (InventoryInfo info : inventories) {
            buf.writeBlockPos(info.pos());
            buf.writeUtf(info.name());
            buf.writeBoolean(info.routable());
            if (info.block() == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                String id = Tools.getId(info.block()).toString();
                buf.writeUtf(id);
            }
        }
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static PacketReturnInventoryInfo create(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<InventoryInfo> inventories = new ArrayList<>(size);
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
        return new PacketReturnInventoryInfo(inventories);
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            GuiStorageScanner.fromServer_inventories = getInventories();
        });
    }

    public record InventoryInfo(BlockPos pos, String name, boolean routable, Block block) {
    }
}