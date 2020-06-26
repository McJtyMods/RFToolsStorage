package mcjty.rftoolsstorage.modules.craftingmanager.system;

import mcjty.rftoolsstorage.modules.craftingmanager.devices.VanillaCraftingDevice;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class CraftingDeviceRegistry {

    private final Map<ResourceLocation, ICraftingDevice> craftingDeviceMap = new HashMap<>();

    public void init() {
        craftingDeviceMap.put(Blocks.CRAFTING_TABLE.getRegistryName(), new VanillaCraftingDevice());
    }

    public void register(ResourceLocation id, ICraftingDevice device) {
        craftingDeviceMap.put(id, device);
    }

    public ICraftingDevice get(ResourceLocation id) {
        return craftingDeviceMap.get(id);
    }
}
