package mcjty.rftoolsstorage.craftinggrid;

import mcjty.rftoolsstorage.RFToolsStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import mcjty.lib.network.PayloadContext;

public record PacketGridToClient(PacketGridSync sync) implements CustomPacketPayload {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(RFToolsStorage.MODID, "gridtoclient");
    public static final CustomPacketPayload.Type<PacketGridToClient> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketGridToClient> CODEC = StreamCodec.composite(
            PacketGridSync.STREAM_CODEC, PacketGridToClient::sync,
            PacketGridToClient::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static PacketGridToClient create(BlockPos pos, ResourceKey<Level> type, CraftingGrid grid) {
        PacketGridSync sync = new PacketGridSync(pos, type, grid);
        return new PacketGridToClient(sync);
    }

    public void handle(PayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            sync.handleMessage(player);
        });
    }
}
