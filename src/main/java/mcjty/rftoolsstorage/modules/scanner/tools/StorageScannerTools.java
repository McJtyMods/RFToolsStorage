package mcjty.rftoolsstorage.modules.scanner.tools;

import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import mcjty.rftoolsstorage.modules.scanner.client.ClientCommandHandler;
import mcjty.rftoolsstorage.setup.RFToolsStorageMessages;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StorageScannerTools {

    public static void scannerSearch(Player player, ResourceKey<Level> dim, BlockPos pos, String text) {
        Level world = LevelTools.getLevel(player.level, dim);
        if (LevelTools.isLoaded(world, pos)) {
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof StorageScannerTileEntity) {
                StorageScannerTileEntity scannerTileEntity = (StorageScannerTileEntity) te;
                Set<BlockPos> inventories = scannerTileEntity.performSearch(text);
                RFToolsStorageMessages.sendToClient(player, ClientCommandHandler.CMD_RETURN_SCANNER_SEARCH,
                        TypedMap.builder()
                                .put(ClientCommandHandler.PARAM_INVENTORIES, new ArrayList<>(inventories)));
            }
        }
    }

    public static void requestContents(Player player, ResourceKey<Level> dim, BlockPos pos, BlockPos invpos) {
        Level world = LevelTools.getLevel(player.level, dim);
        if (LevelTools.isLoaded(world, pos)) {
            BlockEntity te = world.getBlockEntity(pos);
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
