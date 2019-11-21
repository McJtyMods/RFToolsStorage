package mcjty.rftoolsstorage.modules.craftingmanager.system;

import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerTileEntity;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * When the storage scanner requests the craft of an item this happens through requestCraft. What
 * happens then is described here.
 * <p>
 * First a device that is capable of crafting this item is selected. Which device is chosen depends on a few factors:
 * - The device must have a crafting card that can output this item
 * - Devices that are currently idle are preferred
 * - Optional (maybe?): a device that has a 'cheaper' recipe available is preferred. Question: how to calculate the cost of a recipe? A few factors:
 * - Amount of ingredients
 * - Time to craft
 * <p>
 * A CraftingRequest is created. This crafting request contains the desired itemstack as well as all itemstacks that
 * preceeded this request in a crafting chain. When a craft is requested from the storage scanner this chain will be empty.
 * This chain can be clarified better with an example. Say you have a system containing three recipes: one for a redstone
 * torch, one to make redstone from a block of redstone and one to make a block of redstone from redstone. When the storage
 * scanner requests the craft of a redstone torch it will make a CraftingRequest containing an empty chain and the
 * redstone torch as an ItemStack. This is given to the CraftingSystem. The crafting system assigns the request to a
 * crafting manager. This crafting manager checks what items are available in the storage. The following three
 * options are possible:
 * - All items for the recipe in the request are available in storage. These items are fetched and given to the device that
 * supports this recipe. The crafting starts
 * - Some or all of the items are not available but there are existing recipes in the crafting system to make the
 * ingredients. For every one of these ingredients a new crafting request is made which contains the desired
 * ingredient as well as a reference to the current crafting request (as a chain). In this
 * example that could mean a new request for a piece of redstone with the request for the redstone torch added to the chain.
 * The crafting request is suspended in the crafting system
 * - Some or all of the items are not available and no recipes exist for them. The request fails immediatelly
 * <p>
 * The reason for the chain is to prevent loops (in this case between the redstone -> redstone block and redstone block -> redstone
 * recipes). A request will never be granted if it requests something that is already in the chain of the current request.
 * <p>
 * Whenever a crafting manager finishes with a craft it will send the result(s) back to the storage and a general broadcast
 * is sent to all crafting manager that they should possibly continue with suspended requests (can this be optimized?)
 * <p>
 * Optional? every N ticks the crafting system can try resuming some of the suspended requests in case the user inserted
 * needed items into storage manually.
 */
public class CraftingSystem {

    private final StorageScannerTileEntity storage;
    private final Queue<CraftingRequest> queuedRequests = new ArrayDeque<>();
//    private final List<CraftingRequest> runningRequest = new ArrayList<>();
    private final List<CraftingRequest> suspendedRequests = new ArrayList<>();
    private final List<CraftingRequest> failedRequests = new ArrayList<>();

    // Every request has an ID
    private int currentRequestId = 0;
    private final Map<Integer, CraftingRequest> craftingRequestMap = new HashMap<>();

    public CraftingSystem(StorageScannerTileEntity storage) {
        this.storage = storage;
    }

    public void tick(World world) {
        CraftingRequest request = queuedRequests.poll();
        if (request != null) {
            startRequest(world, request);
        }

        boolean checkSuspendedCrafts[] = { false };
        storage.getCraftingInventories().forEach(pos -> {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof CraftingManagerTileEntity) {
                CraftingManagerTileEntity craftingManager = (CraftingManagerTileEntity) te;
                boolean ready = craftingManager.tick(this);
                if (ready) {
                    checkSuspendedCrafts[0] = true;
                }
            }
        });
        if (checkSuspendedCrafts[0]) {
            // One of the crafts is ready so we need to reschedule all suspended crafts
            // @todo optimize this so that only the relevant suspended requests are put back?
            for (CraftingRequest craftingRequest : suspendedRequests) {
                queuedRequests.add(craftingRequest);
            }
            suspendedRequests.clear();
        }
    }

    public StorageScannerTileEntity getStorage() {
        return storage;
    }

    public List<CraftingRequest> getFailedRequests() {
        return failedRequests;
    }

    private static class BestDevice {
        double quality = -1;
        int queue = -1;
        CraftingManagerTileEntity craftingManager;
    }

    private void startRequest(World world, CraftingRequest request) {
        BestDevice bestDevice = storage.getCraftingInventories().stream().collect(BestDevice::new,
                (best, pos) -> {
                    TileEntity te = world.getTileEntity(pos);
                    if (te instanceof CraftingManagerTileEntity) {
                        CraftingManagerTileEntity craftingManager = (CraftingManagerTileEntity) te;
                        Pair<Double, Integer> pair = craftingManager.getCraftingQuality(request.getIngredient(), request.getAmount());
                        Double quality = pair.getLeft();
                        if (quality >= 0 && quality > best.quality) {
                            best.quality = quality;
                            best.queue = pair.getRight();
                            best.craftingManager = craftingManager;
                        }
                    }
                }, (best1, best2) -> {
                });

        if (bestDevice.craftingManager == null) {
            // No crafting manager can craft this. Put the request on the error queue
            failedRequests.add(request);
        } else {
            List<Ingredient> ingredients = bestDevice.craftingManager.getIngredients(bestDevice.queue, request);
            List<ItemStack> extractedItems = storage.requestIngredients(ingredients, ingredient -> {
                // A craft is possible but some items are missing. This consumer is called for every missing ingredient
                CraftingRequest newRequest = new CraftingRequest(newRequestId(), ingredient, 1, request.getId());
                queuedRequests.add(newRequest);
            }, bestDevice.quality >= CraftingManagerTileEntity.QUALITY_DEVICEIDLE);
            if (extractedItems == null) {
                // Some items are missing and no crafters exist to make them. This is an error
                failedRequests.add(request);
            } else if (extractedItems.isEmpty()) {
                // Items are missing or the device is not idle. If crafts are possible they will be requested by the ingredient consumer above
                // Request is suspended so that it can resume later
                suspendedRequests.add(request);
            } else {
                // We have all the needed ingredients and the device is not idle. Start the craft
                if (!bestDevice.craftingManager.startCraft(bestDevice.queue, request, extractedItems)) {
                    // There was a failure. We need to insert the items back into storage
                    rollback(world, extractedItems);
                    failedRequests.add(request);
                } else {
//                    runningRequest.add(request);  // @todo do we need this?
                }
            }
        }
    }

    private void rollback(World world, List<ItemStack> extractedItems) {
        for (ItemStack stack : extractedItems) {
            ItemStack left = storage.insertInternal(stack, false);
            if (!left.isEmpty()) {
                // This could not be inserted. Only thing we can do now is to spawn the item on the ground
                InventoryHelper.spawnItemStack(world, storage.getPos().getX() + .5, storage.getPos().getY() + 1.5, storage.getPos().getZ() + .5,
                        left);
            }
        }
    }

    private int newRequestId() {
        int id = currentRequestId;
        currentRequestId++;
        return id;
    }

    /**
     * Called from the storage scanner: request the craft of the given stack
     */
    public void requestCraft(ItemStack stack, int amount) {
        CraftingRequest request = new CraftingRequest(newRequestId(), Ingredient.fromStacks(stack), amount, -1);
        queuedRequests.add(request);
    }

    public void read(CompoundNBT tag) {
        currentRequestId = tag.getInt("currentRequestId");
        // @todo
    }

    public CompoundNBT write() {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("currentRequestId", currentRequestId);
        // @todo
        return tag;
    }
}
