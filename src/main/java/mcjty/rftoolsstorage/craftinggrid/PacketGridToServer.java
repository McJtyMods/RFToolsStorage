package mcjty.rftoolsstorage.craftinggrid;

import mcjty.lib.varia.DimensionId;
import mcjty.lib.varia.WorldTools;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketGridToServer extends PacketGridSync {

    private ItemStack[] stacks = new ItemStack[0];

    public void toBytes(PacketBuffer buf) {
        convertToBytes(buf);
        buf.writeInt(stacks.length);
        for (ItemStack stack : stacks) {
            buf.writeItem(stack);
        }
    }

    public PacketGridToServer() {
    }

    public PacketGridToServer(PacketBuffer buf) {
        convertFromBytes(buf);
        int len = buf.readInt();
        stacks = new ItemStack[len];
        for (int i = 0 ; i < len ; i++) {
            stacks[i] = buf.readItem();
        }
    }

    public PacketGridToServer(BlockPos pos, RegistryKey<World> type, CraftingGrid grid) {
        init(pos, type, grid);
        stacks = new ItemStack[10];
        for (int i = 0 ; i < 10 ; i++) {
            stacks[i] = grid.getCraftingGridInventory().getStackInSlot(i);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            PlayerEntity player = ctx.getSender();
            World world = player.getCommandSenderWorld();
            CraftingGridProvider provider = handleMessage(WorldTools.getWorld(world, type), player);
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
