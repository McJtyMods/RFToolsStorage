package mcjty.rftoolsstorage.craftinggrid;

import mcjty.lib.varia.SafeClientTools;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketGridToClient extends PacketGridSync {

    public void toBytes(FriendlyByteBuf buf) {
        convertToBytes(buf);
    }

    public PacketGridToClient(FriendlyByteBuf buf) {
        convertFromBytes(buf);
    }

    public PacketGridToClient(BlockPos pos, ResourceKey<Level> type, CraftingGrid grid) {
        init(pos, type, grid);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            Level world = SafeClientTools.getClientWorld();
            Player player = SafeClientTools.getClientPlayer();
            handleMessage(world, player);
        });
        ctx.setPacketHandled(true);
    }
}
