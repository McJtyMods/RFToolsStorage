package mcjty.rftoolsstorage.modules.scanner.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;

import static mcjty.lib.builder.TooltipBuilder.*;

public class StorageScannerBlock extends BaseBlock {

    public StorageScannerBlock() {
        super(new BlockBuilder()
                .tileEntitySupplier(StorageScannerTileEntity::new)
                .infusable()
                .info(key("message.rftoolsstorage.shiftmessage"))
                .infoShift(header(), gold()));
    }
}
