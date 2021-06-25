package mcjty.rftoolsstorage.modules.modularstorage.client;

import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageConfiguration;
import net.minecraft.client.Minecraft;

public class SlotOffsetCalculator {

    // Client side code!
    public static int getYOffset() {
        double height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int ySize;

        if (height > 510) {
            ySize = ModularStorageConfiguration.height3.get();
        } else if (height > 340) {
            ySize = ModularStorageConfiguration.height2.get();
        } else {
            ySize = ModularStorageConfiguration.height1.get();
        }

        return ySize - ModularStorageConfiguration.height1.get();
    }
}
