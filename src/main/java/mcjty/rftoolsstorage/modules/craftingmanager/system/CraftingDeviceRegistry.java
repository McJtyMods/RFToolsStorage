package mcjty.rftoolsstorage.modules.craftingmanager.system;

import mcjty.rftoolsstorage.modules.craftingmanager.devices.VanillaCraftingDevice;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CraftingDeviceRegistry {

    private final Map<ResourceLocation, Supplier<ICraftingDevice>> craftingDeviceMap = new HashMap<>();

    public void init() {
        craftingDeviceMap.put(Blocks.CRAFTING_TABLE.getRegistryName(), VanillaCraftingDevice::new);
    }

    public void register(ResourceLocation id, Supplier<ICraftingDevice> device) {
        craftingDeviceMap.put(id, device);
    }

    public Supplier<ICraftingDevice> get(ResourceLocation id) {
        return craftingDeviceMap.get(id);
    }
}
