package mcjty.rftoolsstorage.modules.craftingmanager.system;

import mcjty.rftoolsstorage.modules.craftingmanager.devices.VanillaCraftingDevice;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CraftingDeviceRegistry {

    private final Map<ResourceLocation, ResourceLocation> blockToDeviceMap = new HashMap<>();
    private final Map<ResourceLocation, Supplier<ICraftingDevice>> deviceToSupplierMap = new HashMap<>();

    public void init() {
        register(Blocks.CRAFTING_TABLE.getRegistryName(), VanillaCraftingDevice.DEVICE_VANILLA_CRAFTING, VanillaCraftingDevice::new);
    }

    public void register(ResourceLocation blockId, ResourceLocation deviceId, Supplier<ICraftingDevice> device) {
        blockToDeviceMap.put(blockId, deviceId);
        deviceToSupplierMap.put(deviceId, device);
    }

    public ResourceLocation getDeviceForBlock(ResourceLocation blockId) {
        return blockToDeviceMap.get(blockId);
    }

    public Supplier<ICraftingDevice> getDeviceSupplier(ResourceLocation deviceId) {
        return deviceToSupplierMap.get(deviceId);
    }
}
