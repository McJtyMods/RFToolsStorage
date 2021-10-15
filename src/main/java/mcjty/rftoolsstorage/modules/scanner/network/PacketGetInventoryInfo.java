package mcjty.rftoolsstorage.modules.scanner.network;


import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.DimensionId;
import mcjty.lib.varia.WorldTools;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageTileEntity;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import mcjty.rftoolsstorage.setup.RFToolsStorageMessages;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PacketGetInventoryInfo {

    private RegistryKey<World> id;
    private BlockPos pos;
    private boolean doscan;

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeResourceLocation(id.location());
        buf.writeBoolean(doscan);
    }

    public PacketGetInventoryInfo() {
    }

    public PacketGetInventoryInfo(PacketBuffer buf) {
        pos = buf.readBlockPos();
        id = RegistryKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation());
        doscan = buf.readBoolean();
    }

    public PacketGetInventoryInfo(RegistryKey<World> worldId, BlockPos pos, boolean doscan) {
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

    private void sendReplyToClient(List<PacketReturnInventoryInfo.InventoryInfo> reply, ServerPlayerEntity player) {
        PacketReturnInventoryInfo msg = new PacketReturnInventoryInfo(reply);
        RFToolsStorageMessages.INSTANCE.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    private Optional<List<PacketReturnInventoryInfo.InventoryInfo>> onMessageServer(PlayerEntity entityPlayerMP) {
        World world = WorldTools.getWorld(entityPlayerMP.level, id);
        if (world == null) {
            return Optional.empty();
        }

        if (!WorldTools.isLoaded(world, pos)) {
            return Optional.empty();
        }

        TileEntity te = world.getBlockEntity(pos);
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

    private static PacketReturnInventoryInfo.InventoryInfo toInventoryInfo(World world, BlockPos pos, StorageScannerTileEntity te) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        String displayName;
        if (!WorldTools.isLoaded(world, pos)) {
            displayName = "[UNLOADED]";
            block = null;
        } else if (world.isEmptyBlock(pos)) {
            displayName = "[REMOVED]";
            block = null;
        } else {
            displayName = BlockTools.getReadableName(world, pos);
            TileEntity storageTe = world.getBlockEntity(pos);
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
