package mcjty.rftoolsstorage.modules.scanner.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageTileEntity;

public class StorageScannerBlock extends BaseBlock {

    public StorageScannerBlock() {
        super("storage_scanner", new BlockBuilder()
                .tileEntitySupplier(ModularStorageTileEntity::new)
                .infusable()
                .infoExtended("todo"));   // @todo 1.14

        // @todo 1.14
//        list.add(TextFormatting.WHITE + "This machine will scan all nearby inventories");
//        list.add(TextFormatting.WHITE + "and show them in a list. You can then search");
//        list.add(TextFormatting.WHITE + "for items in all those inventories.");
//        list.add(TextFormatting.YELLOW + "Infusing bonus: reduced power consumption.");
    }
}
