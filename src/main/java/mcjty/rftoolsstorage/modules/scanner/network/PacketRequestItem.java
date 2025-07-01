package mcjty.rftoolsstorage.modules.scanner.network;

import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketRequestItem(ResourceKey<Level> dimensionId, BlockPos pos, BlockPos inventoryPos, ItemStack item, Integer amount, Boolean craftable) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsStorage.MODID, "requestitem");
    public static final Type<PacketRequestItem> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketRequestItem> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), PacketRequestItem::dimensionId,
            BlockPos.STREAM_CODEC, PacketRequestItem::pos,
            BlockPos.STREAM_CODEC, PacketRequestItem::inventoryPos,
            ItemStack.OPTIONAL_STREAM_CODEC, PacketRequestItem::item,
            ByteBufCodecs.INT, PacketRequestItem::amount,
            ByteBufCodecs.BOOL, PacketRequestItem::craftable,
            PacketRequestItem::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static PacketRequestItem create(ResourceKey<Level>
                                     dimensionId, BlockPos pos, BlockPos inventoryPos, ItemStack item, int amount, boolean craftable) {
        return new PacketRequestItem(dimensionId, pos, inventoryPos, item, amount, craftable);
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            Level world = LevelTools.getLevel(player.level(), dimensionId);
            if (world == null) {
                return;
            }
            if (!LevelTools.isLoaded(world, pos)) {
                return;
            }
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof StorageScannerTileEntity scanner) {
                if (craftable) {
                    scanner.requestCraft(inventoryPos, item, amount, player);
                } else {
                    scanner.requestStack(inventoryPos, item, amount, player);
                }
            }
        });
    }
}
