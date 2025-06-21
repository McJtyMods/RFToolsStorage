package mcjty.rftoolsstorage.craftinggrid;

import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsstorage.RFToolsStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record PacketGridToServer(PacketGridSync sync, List<ItemStack> stacks) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsStorage.MODID, "gridtoserver");
    public static final CustomPacketPayload.Type<PacketGridToServer> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketGridToServer> CODEC = StreamCodec.composite(
            PacketGridSync.STREAM_CODEC, PacketGridToServer::sync,
            ItemStack.OPTIONAL_LIST_STREAM_CODEC, PacketGridToServer::stacks,
            PacketGridToServer::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static PacketGridToServer create(BlockPos pos, ResourceKey<Level> type, CraftingGrid grid) {
        PacketGridSync sync = new PacketGridSync(pos, type, grid);
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0 ; i < 10 ; i++) {
            stacks.add(grid.getCraftingGridInventory().getStackInSlot(i));
        }
        return new PacketGridToServer(sync, stacks);
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            Level world = player.getCommandSenderWorld();
            CraftingGridProvider provider = sync.handleMessage(LevelTools.getLevel(world, sync.type()), player);
            if (provider != null) {
                CraftingGridInventory inventory = provider.getCraftingGrid().getCraftingGridInventory();
                for (int i = 0; i < 10; i++) {
                    inventory.setStackInSlot(i, stacks.get(i));
                }
                provider.markInventoryDirty();
            }
        });
    }
}
