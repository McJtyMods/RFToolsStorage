package mcjty.rftoolsstorage.craftinggrid;

import io.netty.buffer.ByteBuf;
import mcjty.lib.McJtyLib;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketGridToClient extends PacketGridSync {

    public void fromBytes(ByteBuf buf) {
        convertFromBytes(buf);
    }

    public void toBytes(ByteBuf buf) {
        convertToBytes(buf);
    }

    public PacketGridToClient() {
    }

    public PacketGridToClient(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketGridToClient(BlockPos pos, CraftingGrid grid) {
        init(pos, grid);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            World world = McJtyLib.proxy.getClientWorld();
            PlayerEntity player = McJtyLib.proxy.getClientPlayer();
            handleMessage(world, player);
        });
        ctx.setPacketHandled(true);
    }
}
