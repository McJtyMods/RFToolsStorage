package mcjty.rftoolsstorage.modules.scanner.network;


import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageTileEntity;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import mcjty.rftoolsstorage.setup.RFToolsStorageMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record PacketGetInventoryInfo(ResourceKey<Level> levelId, BlockPos pos, boolean doscan) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsStorage.MODID, "getinventoryinfo");

    public static PacketGetInventoryInfo create(ResourceKey<Level> dimension, BlockPos storageScannerPos, boolean b) {
        return new PacketGetInventoryInfo(dimension, storageScannerPos, b);
    }

    public static PacketGetInventoryInfo create(FriendlyByteBuf buf) {
        return new PacketGetInventoryInfo(LevelTools.getId(buf.readResourceLocation()), buf.readBlockPos(), buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(levelId.location());
        buf.writeBlockPos(pos);
        buf.writeBoolean(doscan);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(player -> {
                onMessageServer(player).ifPresent(p -> sendReplyToClient(p, (ServerPlayer) player));
            });
        });
    }

    private void sendReplyToClient(List<PacketReturnInventoryInfo.InventoryInfo> reply, ServerPlayer player) {
        PacketReturnInventoryInfo msg = new PacketReturnInventoryInfo(reply);
        RFToolsStorageMessages.sendToPlayer(msg, player);
    }

    private Optional<List<PacketReturnInventoryInfo.InventoryInfo>> onMessageServer(Player player) {
        Level world = LevelTools.getLevel(player.level(), levelId);
        if (world == null) {
            return Optional.empty();
        }

        if (!LevelTools.isLoaded(world, pos)) {
            return Optional.empty();
        }

        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof StorageScannerTileEntity scanner) {
            Stream<BlockPos> inventories;
            if (doscan) {
                inventories = scanner.findInventories();
            } else {
                inventories = scanner.getAllInventories();
            }

            List<PacketReturnInventoryInfo.InventoryInfo> invs = inventories
                    .map(pos -> toInventoryInfo(world, pos, scanner))
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
            if (storageTe instanceof ModularStorageTileEntity storage) {
                String finalDisplayName = displayName;
                displayName = storage.getCapability(ForgeCapabilities.ITEM_HANDLER).map(h -> {
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
