package mcjty.rftoolsstorage.craftinggrid;

import mcjty.lib.varia.LevelTools;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketGridToServer extends PacketGridSync {

    private ItemStack[] stacks = new ItemStack[0];

    public void toBytes(FriendlyByteBuf buf) {
        convertToBytes(buf);
        buf.writeInt(stacks.length);
        for (ItemStack stack : stacks) {
            buf.writeItem(stack);
        }
    }

    public PacketGridToServer() {
    }

    public PacketGridToServer(FriendlyByteBuf buf) {
        convertFromBytes(buf);
        int len = buf.readInt();
        stacks = new ItemStack[len];
        for (int i = 0 ; i < len ; i++) {
            stacks[i] = buf.readItem();
        }
    }

    public PacketGridToServer(BlockPos pos, ResourceKey<Level> type, CraftingGrid grid) {
        init(pos, type, grid);
        stacks = new ItemStack[10];
        for (int i = 0 ; i < 10 ; i++) {
            stacks[i] = grid.getCraftingGridInventory().getStackInSlot(i);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            Player player = ctx.getSender();
            Level world = player.getCommandSenderWorld();
            CraftingGridProvider provider = handleMessage(LevelTools.getLevel(world, type), player);
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
