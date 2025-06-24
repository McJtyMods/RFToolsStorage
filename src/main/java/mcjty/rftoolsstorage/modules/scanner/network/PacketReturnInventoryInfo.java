package mcjty.rftoolsstorage.modules.scanner.network;


import mcjty.lib.varia.Tools;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.scanner.client.GuiStorageScanner;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Optional;

public record PacketReturnInventoryInfo(List<InventoryInfo> inventories) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsStorage.MODID, "return_inventory_info");
    public static final CustomPacketPayload.Type<PacketReturnInventoryInfo> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacketReturnInventoryInfo> CODEC = StreamCodec.composite(
            InventoryInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), PacketReturnInventoryInfo::inventories,
            PacketReturnInventoryInfo::new
    );

    public List<InventoryInfo> getInventories() {
        return inventories;
    }


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            GuiStorageScanner.fromServer_inventories = getInventories();
        });
    }

    public record InventoryInfo(BlockPos pos, String name, boolean routable, Block block) {
        public static final StreamCodec<FriendlyByteBuf, InventoryInfo> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, InventoryInfo::pos,
                ByteBufCodecs.STRING_UTF8, InventoryInfo::name,
                ByteBufCodecs.BOOL, InventoryInfo::routable,
                ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), s -> s.block == null ? Optional.empty() : Optional.of(Tools.getId(s.block)),
                (pos, name, routable, blockId) -> new InventoryInfo(
                        pos,
                        name,
                        routable,
                        blockId.map(Tools::getBlock).orElse(null)
                )
        );
    }
}