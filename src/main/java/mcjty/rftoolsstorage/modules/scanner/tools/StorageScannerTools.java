package mcjty.rftoolsstorage.modules.scanner.tools;

import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.WorldTools;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import mcjty.rftoolsstorage.modules.scanner.client.ClientCommandHandler;
import mcjty.rftoolsstorage.setup.RFToolsStorageMessages;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StorageScannerTools {

    public static void scannerSearch(PlayerEntity player, RegistryKey<World> dim, BlockPos pos, String text) {
        World world = WorldTools.getLevel(player.level, dim);
        if (WorldTools.isLoaded(world, pos)) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof StorageScannerTileEntity) {
                StorageScannerTileEntity scannerTileEntity = (StorageScannerTileEntity) te;
                Set<BlockPos> inventories = scannerTileEntity.performSearch(text);
                RFToolsStorageMessages.sendToClient(player, ClientCommandHandler.CMD_RETURN_SCANNER_SEARCH,
                        TypedMap.builder()
                                .put(ClientCommandHandler.PARAM_INVENTORIES, new ArrayList<>(inventories)));
            }
        }
    }

    public static void requestContents(PlayerEntity player, RegistryKey<World> dim, BlockPos pos, BlockPos invpos) {
        World world = WorldTools.getLevel(player.level, dim);
        if (WorldTools.isLoaded(world, pos)) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof StorageScannerTileEntity) {
                StorageScannerTileEntity scannerTileEntity = (StorageScannerTileEntity) te;
                List<ItemStack> stacks = new ArrayList<>();
                List<ItemStack> craftable = new ArrayList<>();
                scannerTileEntity.getInventoryForBlock(invpos, stacks, craftable);
                RFToolsStorageMessages.sendToClient(player, ClientCommandHandler.CMD_RETURN_SCANNER_CONTENTS,
                        TypedMap.builder()
                                .put(ClientCommandHandler.PARAM_STACKS, stacks)
                                .put(ClientCommandHandler.PARAM_CRAFTABLE, craftable)
                );
            }
        }
    }
}
