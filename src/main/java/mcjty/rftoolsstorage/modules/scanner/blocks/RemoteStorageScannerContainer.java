package mcjty.rftoolsstorage.modules.scanner.blocks;

import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.math.BlockPos;

public class RemoteStorageScannerContainer extends StorageScannerContainer {

    public RemoteStorageScannerContainer(ContainerType<StorageScannerContainer> type, int id, BlockPos pos, StorageScannerTileEntity tileEntity) {
        super(type, id, pos, tileEntity);
    }

    @Override
    protected boolean isRemoteContainer() {
        return true;
    }
}
