package mcjty.rftoolsstorage.craftinggrid;

import mcjty.lib.varia.SafeClientTools;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketGridToClient extends PacketGridSync {

    public void toBytes(PacketBuffer buf) {
        convertToBytes(buf);
    }

    public PacketGridToClient() {
    }

    public PacketGridToClient(PacketBuffer buf) {
        convertFromBytes(buf);
    }

    public PacketGridToClient(BlockPos pos, RegistryKey<World> type, CraftingGrid grid) {
        init(pos, type, grid);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            World world = SafeClientTools.getClientWorld();
            PlayerEntity player = SafeClientTools.getClientPlayer();
            handleMessage(world, player);
        });
        ctx.setPacketHandled(true);
    }
}
