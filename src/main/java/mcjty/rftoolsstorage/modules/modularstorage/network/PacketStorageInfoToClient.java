package mcjty.rftoolsstorage.modules.modularstorage.network;

import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageTileEntity;
import mcjty.rftoolsstorage.modules.scanner.tools.SortingMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketStorageInfoToClient(BlockPos pos, String sortMode, String viewMode, Boolean groupMode, String filter, Boolean locked) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsStorage.MODID, "storageinfotoclient");
    public static final CustomPacketPayload.Type<PacketStorageInfoToClient> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacketStorageInfoToClient> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketStorageInfoToClient::pos,
            ByteBufCodecs.STRING_UTF8, PacketStorageInfoToClient::sortMode,
            ByteBufCodecs.STRING_UTF8, PacketStorageInfoToClient::viewMode,
            ByteBufCodecs.BOOL, PacketStorageInfoToClient::groupMode,
            ByteBufCodecs.STRING_UTF8, PacketStorageInfoToClient::filter,
            ByteBufCodecs.BOOL, PacketStorageInfoToClient::locked,
            PacketStorageInfoToClient::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static PacketStorageInfoToClient create(BlockPos pos,
                                     String sortMode, String viewMode, boolean groupMode, String filter, boolean locked) {
        return new PacketStorageInfoToClient(pos, sortMode, viewMode, groupMode, filter, locked);
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            BlockEntity te = SafeClientTools.getClientWorld().getBlockEntity(pos);
            if (te instanceof ModularStorageTileEntity storage) {
                storage.syncInventoryFromServer(sortMode, viewMode, groupMode, filter, locked);
            }
        });
    }
}
