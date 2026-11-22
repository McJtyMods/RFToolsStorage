package mcjty.rftoolsstorage.modules.scanner.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftoolsbase.tools.ManualHelper;

import static mcjty.lib.builder.TooltipBuilder.*;

public class StorageScannerBlock extends BaseBlock {

    public StorageScannerBlock(Properties properties) {
        super(new BlockBuilder()
                .properties(properties)
                .tileEntitySupplier(StorageScannerTileEntity::new)
                .infusable()
                .manualEntry(ManualHelper.create("rftoolsbase:scanner/scanner"))
                .info(key("message.rftoolsstorage.shiftmessage"))
                .infoShift(header(), gold()));
    }
}
