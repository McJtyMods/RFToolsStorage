package mcjty.rftoolsstorage.modules.craftingmanager.system;

import mcjty.lib.varia.Tools;
import mcjty.rftoolsstorage.modules.craftingmanager.devices.VanillaCraftingDevice;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CraftingDeviceRegistry {

    private final Map<Identifier, Identifier> blockToDeviceMap = new HashMap<>();
    private final Map<Identifier, Supplier<ICraftingDevice>> deviceToSupplierMap = new HashMap<>();

    public void init() {
        register(Tools.getId(Blocks.CRAFTING_TABLE), VanillaCraftingDevice.DEVICE_VANILLA_CRAFTING, VanillaCraftingDevice::new);
    }

    public void register(Identifier blockId, Identifier deviceId, Supplier<ICraftingDevice> device) {
        blockToDeviceMap.put(blockId, deviceId);
        deviceToSupplierMap.put(deviceId, device);
    }

    public Identifier getDeviceForBlock(Identifier blockId) {
        return blockToDeviceMap.get(blockId);
    }

    public Supplier<ICraftingDevice> getDeviceSupplier(Identifier deviceId) {
        return deviceToSupplierMap.get(deviceId);
    }
}
