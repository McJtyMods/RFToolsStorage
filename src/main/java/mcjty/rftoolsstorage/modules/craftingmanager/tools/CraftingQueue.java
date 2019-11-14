package mcjty.rftoolsstorage.modules.craftingmanager.tools;

import java.util.ArrayDeque;
import java.util.Queue;

public class CraftingQueue {

    private ICraftingDevice device;
    private Queue<CraftingRequest> requests = new ArrayDeque<>();

    public ICraftingDevice getDevice() {
        return device;
    }

    public void setDevice(ICraftingDevice device) {
        this.device = device;
    }

    public Queue<CraftingRequest> getRequests() {
        return requests;
    }

    public boolean hasDevice() {
        return device != null;
    }
}
