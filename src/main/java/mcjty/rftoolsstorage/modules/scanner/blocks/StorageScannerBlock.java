package mcjty.rftoolsstorage.modules.scanner.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftoolsbase.tools.ManualHelper;

import static mcjty.lib.builder.TooltipBuilder.*;

public class StorageScannerBlock extends BaseBlock {

    public StorageScannerBlock() {
        super(new BlockBuilder()
                .tileEntitySupplier(StorageScannerTileEntity::new)
                .infusable()
                .manualEntry(ManualHelper.create("rftoolsstorage:scanner/scanner"))
                .info(key("message.rftoolsstorage.shiftmessage"))
                .infoShift(header(), gold()));
    }
}
