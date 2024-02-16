package mcjty.rftoolsstorage.craftinggrid;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsstorage.RFToolsStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record PacketGridToServer(PacketGridSync sync, ItemStack[] stacks) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsStorage.MODID, "gridtoserver");

    @Override
    public void write(FriendlyByteBuf buf) {
        sync.convertToBytes(buf);
        buf.writeInt(stacks.length);
        for (ItemStack stack : stacks) {
            buf.writeItem(stack);
        }
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static PacketGridToServer create(FriendlyByteBuf buf) {
        PacketGridSync sync = new PacketGridSync();
        sync.convertFromBytes(buf);
        int len = buf.readInt();
        ItemStack[] stacks = new ItemStack[len];
        for (int i = 0 ; i < len ; i++) {
            stacks[i] = buf.readItem();
        }
        return new PacketGridToServer(sync, stacks);
    }

    public static PacketGridToServer create(BlockPos pos, ResourceKey<Level> type, CraftingGrid grid) {
        PacketGridSync sync = new PacketGridSync();
        sync.init(pos, type, grid);
        ItemStack[] stacks = new ItemStack[10];
        for (int i = 0 ; i < 10 ; i++) {
            stacks[i] = grid.getCraftingGridInventory().getStackInSlot(i);
        }
        return new PacketGridToServer(sync, stacks);
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(player -> {
                Level world = player.getCommandSenderWorld();
                CraftingGridProvider provider = sync.handleMessage(LevelTools.getLevel(world, sync.type), player);
                if (provider != null) {
                    CraftingGridInventory inventory = provider.getCraftingGrid().getCraftingGridInventory();
                    for (int i = 0; i < 10; i++) {
                        inventory.setStackInSlot(i, stacks[i]);
                    }
                    provider.markInventoryDirty();
                }
            });
        });
    }
}
