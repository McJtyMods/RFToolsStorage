package mcjty.rftoolsstorage.modules.scanner.items;

import mcjty.lib.gui.ManualEntry;
import mcjty.rftoolsbase.modules.tablet.items.TabletItem;
import mcjty.rftoolsbase.tools.ManualHelper;

public class StorageScannerTabletItem extends TabletItem {

    @Override
    public ManualEntry getManualEntry() {
        return ManualHelper.create("rftoolsstorage:scanner/remote");
    }
}
