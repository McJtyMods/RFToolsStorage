package mcjty.rftoolsstorage.craftinggrid;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsstorage.RFToolsStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public record PacketGridToClient(PacketGridSync sync) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsStorage.MODID, "gridtoclient");

    @Override
    public void write(FriendlyByteBuf buf) {
        sync.convertToBytes(buf);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static PacketGridToClient create(FriendlyByteBuf buf) {
        PacketGridSync sync = new PacketGridSync();
        sync.convertFromBytes(buf);
        return new PacketGridToClient(sync);
    }

    public static PacketGridToClient create(BlockPos pos, ResourceKey<Level> type, CraftingGrid grid) {
        PacketGridSync sync = new PacketGridSync();
        sync.init(pos, type, grid);
        return new PacketGridToClient(sync);
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            Level world = SafeClientTools.getClientWorld();
            Player player = SafeClientTools.getClientPlayer();
            sync.handleMessage(world, player);
        });
    }
}
