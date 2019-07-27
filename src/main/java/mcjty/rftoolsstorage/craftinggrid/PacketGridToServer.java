package mcjty.rftoolsstorage.craftinggrid;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketGridToServer extends PacketGridSync {

    private ItemStack[] stacks = new ItemStack[0];

    public void fromBytes(ByteBuf buf) {
        convertFromBytes(buf);
        int len = buf.readInt();
        stacks = new ItemStack[len];
        for (int i = 0 ; i < len ; i++) {
            if (buf.readBoolean()) {
                stacks[i] = NetworkTools.readItemStack(buf);
            } else {
                stacks[i] = ItemStack.EMPTY;
            }
        }
    }

    public void toBytes(ByteBuf buf) {
        convertToBytes(buf);
        buf.writeInt(stacks.length);
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                buf.writeBoolean(true);
                NetworkTools.writeItemStack(buf, stack);
            } else {
                buf.writeBoolean(false);
            }
        }

    }

    public PacketGridToServer() {
    }

    public PacketGridToServer(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketGridToServer(BlockPos pos, CraftingGrid grid) {
        init(pos, grid);
        stacks = new ItemStack[10];
        for (int i = 0 ; i < 10 ; i++) {
            stacks[i] = grid.getCraftingGridInventory().getStackInSlot(i);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            PlayerEntity player = ctx.getSender();
            World world = player.getEntityWorld();
            CraftingGridProvider provider = handleMessage(world, player);
            if (provider != null) {
                CraftingGridInventory inventory = provider.getCraftingGrid().getCraftingGridInventory();
                for (int i = 0 ; i < 10 ; i++) {
                    inventory.setStackInSlot(i, stacks[i]);
                }
                provider.markInventoryDirty();
            }
        });
        ctx.setPacketHandled(true);
    }
}
