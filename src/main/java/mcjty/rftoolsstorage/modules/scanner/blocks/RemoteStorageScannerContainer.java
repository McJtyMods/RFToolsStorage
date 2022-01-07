package mcjty.rftoolsstorage.modules.scanner.blocks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class RemoteStorageScannerContainer extends StorageScannerContainer {

    public RemoteStorageScannerContainer(ContainerType<StorageScannerContainer> type, int id, BlockPos pos, StorageScannerTileEntity tileEntity, @Nonnull PlayerEntity player) {
        super(type, id, pos, tileEntity, player);
    }

    @Override
    protected boolean isRemoteContainer() {
        return true;
    }
}
