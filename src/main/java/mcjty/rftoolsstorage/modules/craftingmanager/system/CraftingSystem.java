package mcjty.rftoolsstorage.modules.craftingmanager.system;

import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * When the storage scanner requests the craft of an item this happens through requestCraft. What
 * happens then is described here.
 *
 * First a device that is capable of crafting this item is selected. Which device is chosen depends on a few factors:
 *   - The device must have a crafting card that can output this item
 *   - Devices that are currently idle are preferred
 *   - Optional (maybe?): a device that has a 'cheaper' recipe available is preferred. Question: how to calculate the cost of a recipe? A few factors:
 *       - Amount of ingredients
 *       - Time to craft
 *
 * A CraftingRequest is created. This crafting request contains the desired itemstack as well as all itemstacks that
 * preceeded this request in a crafting chain. When a craft is requested from the storage scanner this chain will be empty.
 * This chain can be clarified better with an example. Say you have a system containing three recipes: one for a redstone
 * torch, one to make redstone from a block of redstone and one to make a block of redstone from redstone. When the storage
 * scanner requests the craft of a redstone torch it will make a CraftingRequest containing an empty chain and the
 * redstone torch as an ItemStack. This is given to the CraftingSystem. The crafting system assigns the request to a
 * crafting manager. This crafting manager checks what items are available in the storage. The following three
 * options are possible:
 *     - All items for the recipe in the request are available in storage. These items are fetched and given to the device that
 *       supports this recipe. The crafting starts
 *     - Some or all of the items are not available but there are existing recipes in the crafting system to make the
 *       ingredients. For every one of these ingredients a new crafting request is made which contains the desired
 *       ingredient as well as a reference to the current crafting request (as a chain). In this
 *       example that could mean a new request for a piece of redstone with the request for the redstone torch added to the chain.
 *       The crafting request is suspended in the crafting system
 *     - Some or all of the items are not available and no recipes exist for them. The request fails immediatelly
 *
 * The reason for the chain is to prevent loops (in this case between the redstone -> redstone block and redstone block -> redstone
 * recipes). A request will never be granted if it requests something that is already in the chain of the current request.
 *
 * Whenever a crafting manager finishes with a craft it will send the result(s) back to the storage and a general broadcast
 * is sent to all crafting manager that they should possibly continue with suspended requests (can this be optimized?)
 *
 * Optional? every N ticks the crafting system can try resuming some of the suspended requests in case the user inserted
 * needed items into storage manually.
 */
public class CraftingSystem {

    private final StorageScannerTileEntity storage;
    private final Queue<CraftingRequest> queuedRequests = new ArrayDeque<>();
    private final List<CraftingRequest> suspendedRequests = new ArrayList<>();
    private final List<CraftingRequest> failedRequests = new ArrayList<>();

    public CraftingSystem(StorageScannerTileEntity storage) {
        this.storage = storage;
    }

    public void tick() {
        CraftingRequest request = queuedRequests.poll();
        if (request != null) {
            startRequest(request);
        }
    }

    private void startRequest(CraftingRequest request) {
        // @todo
    }

    /**
     * Called from the storage scanner: request the craft of the given stack
     */
    public void requestCraft(ItemStack stack, int amount) {
        CraftingRequest request = new CraftingRequest(stack, amount, null);
        queuedRequests.add(request);
    }

    public void read(CompoundNBT tag) {
    }

    public CompoundNBT write() {
        CompoundNBT tag = new CompoundNBT();
        return tag;
    }
}
