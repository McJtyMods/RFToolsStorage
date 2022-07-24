package mcjty.rftoolsstorage.modules.scanner.blocks;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;

public class RemoteStorageScannerContainer extends StorageScannerContainer {

    public RemoteStorageScannerContainer(MenuType<RemoteStorageScannerContainer> type, int id, BlockPos pos, StorageScannerTileEntity tileEntity, @Nonnull Player player) {
        super(type, id, pos, tileEntity, player);
    }

    @Override
    protected boolean isRemoteContainer() {
        return true;
    }
}
