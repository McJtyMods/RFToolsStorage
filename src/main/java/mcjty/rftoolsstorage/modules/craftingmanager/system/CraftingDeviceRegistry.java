package mcjty.rftoolsstorage.modules.craftingmanager.system;

import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class CraftingDeviceRegistry {

    private final Map<ResourceLocation, ICraftingDevice> craftingDeviceMap = new HashMap<>();

    public void init() {

    }

    public void register(ResourceLocation id, ICraftingDevice device) {
        craftingDeviceMap.put(id, device);
    }

    public ICraftingDevice get(ResourceLocation id) {
        return craftingDeviceMap.get(id);
    }
}
