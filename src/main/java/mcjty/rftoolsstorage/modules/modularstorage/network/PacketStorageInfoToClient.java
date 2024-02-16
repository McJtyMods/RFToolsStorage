package mcjty.rftoolsstorage.modules.modularstorage.network;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public record PacketStorageInfoToClient(BlockPos pos, String sortMode, String viewMode, Boolean groupMode, String filter, Boolean locked) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsStorage.MODID, "storageinfotoclient");

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(viewMode);
        buf.writeUtf(sortMode);
        buf.writeBoolean(groupMode);
        buf.writeUtf(filter);
        buf.writeBoolean(locked);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static PacketStorageInfoToClient create(FriendlyByteBuf buf) {
        return new PacketStorageInfoToClient(buf.readBlockPos(), buf.readUtf(32767), buf.readUtf(32767), buf.readBoolean(), buf.readUtf(32767), buf.readBoolean());
    }

    public static PacketStorageInfoToClient create(BlockPos pos,
                                     String sortMode, String viewMode, boolean groupMode, String filter, boolean locked) {
        return new PacketStorageInfoToClient(pos, sortMode, viewMode, groupMode, filter, locked);
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            BlockEntity te = SafeClientTools.getClientWorld().getBlockEntity(pos);
            if (te instanceof ModularStorageTileEntity storage) {
                storage.syncInventoryFromServer(sortMode, viewMode, groupMode, filter, locked);
            }
        });
    }
}
