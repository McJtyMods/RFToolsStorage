package mcjty.rftoolsstorage.modules.scanner.network;


import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageTileEntity;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import mcjty.rftoolsstorage.setup.RFToolsStorageMessages;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PacketGetInventoryInfo {

    private ResourceKey<Level> id;
    private BlockPos pos;
    private boolean doscan;

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeResourceLocation(id.location());
        buf.writeBoolean(doscan);
    }

    public PacketGetInventoryInfo() {
    }

    public PacketGetInventoryInfo(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        id = LevelTools.getId(buf.readResourceLocation());
        doscan = buf.readBoolean();
    }

    public PacketGetInventoryInfo(ResourceKey<Level> worldId, BlockPos pos, boolean doscan) {
        this.id = worldId;
        this.pos = pos;
        this.doscan = doscan;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            onMessageServer(ctx.getSender())
                    .ifPresent(p -> sendReplyToClient(p, ctx.getSender()));
        });
        ctx.setPacketHandled(true);
    }

    private void sendReplyToClient(List<PacketReturnInventoryInfo.InventoryInfo> reply, ServerPlayer player) {
        PacketReturnInventoryInfo msg = new PacketReturnInventoryInfo(reply);
        RFToolsStorageMessages.INSTANCE.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    private Optional<List<PacketReturnInventoryInfo.InventoryInfo>> onMessageServer(Player entityPlayerMP) {
        Level world = LevelTools.getLevel(entityPlayerMP.level, id);
        if (world == null) {
            return Optional.empty();
        }

        if (!LevelTools.isLoaded(world, pos)) {
            return Optional.empty();
        }

        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof StorageScannerTileEntity) {
            StorageScannerTileEntity scannerTileEntity = (StorageScannerTileEntity) te;
            Stream<BlockPos> inventories;
            if (doscan) {
                inventories = scannerTileEntity.findInventories();
            } else {
                inventories = scannerTileEntity.getAllInventories();
            }

            List<PacketReturnInventoryInfo.InventoryInfo> invs = inventories
                    .map(pos -> toInventoryInfo(world, pos, scannerTileEntity))
                    .collect(Collectors.toList());

            return Optional.of(invs);
        }

        return Optional.empty();
    }

    private static PacketReturnInventoryInfo.InventoryInfo toInventoryInfo(Level world, BlockPos pos, StorageScannerTileEntity te) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        String displayName;
        if (!LevelTools.isLoaded(world, pos)) {
            displayName = "[UNLOADED]";
            block = null;
        } else if (world.isEmptyBlock(pos)) {
            displayName = "[REMOVED]";
            block = null;
        } else {
            displayName = Tools.getReadableName(world, pos);
            BlockEntity storageTe = world.getBlockEntity(pos);
            if (storageTe instanceof ModularStorageTileEntity) {
                ModularStorageTileEntity storageTileEntity = (ModularStorageTileEntity) storageTe;
                String finalDisplayName = displayName;
                displayName = storageTileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(h -> {
                    ItemStack storageModule = h.getStackInSlot(ModularStorageContainer.SLOT_STORAGE_MODULE);
                    if (!storageModule.isEmpty()) {
                        if (storageModule.hasTag() && storageModule.getTag().contains("display")) {
                            return storageModule.getHoverName().getString() /* was getFormattedText() */;
                        }
                    }
                    return finalDisplayName;
                }).orElse(displayName);
            }
        }
        return new PacketReturnInventoryInfo.InventoryInfo(pos, displayName, te.isRoutable(pos), block);
    }

}
