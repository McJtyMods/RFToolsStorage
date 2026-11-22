package mcjty.rftoolsstorage.modules.craftingmanager.blocks;

import com.google.common.util.concurrent.AtomicDouble;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.modules.crafting.items.CraftingCardItem;
import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerModule;
import mcjty.rftoolsstorage.modules.craftingmanager.system.CraftingQueue;
import mcjty.rftoolsstorage.modules.craftingmanager.system.CraftingRequest;
import mcjty.rftoolsstorage.modules.craftingmanager.system.CraftingSystem;
import mcjty.rftoolsstorage.modules.craftingmanager.system.ICraftingDevice;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

public class CraftingManagerTileEntity extends GenericTileEntity {


    private final GenericItemHandler items = GenericItemHandler.create(this, CraftingManagerContainer.CONTAINER_FACTORY)
            .onUpdate((slot, stack) -> {
                if (slot < 4) {
                    markDirtyClient();
                    devicesDirty = true;
                }
            })
            .build();
    @Cap(type = CapType.ITEMS_AUTOMATION)
    private static final Function<CraftingManagerTileEntity, GenericItemHandler> ITEM_CAP = be -> be.items;


    @Cap(type = CapType.CONTAINER)
    private static final Function<CraftingManagerTileEntity, MenuProvider> screenHandler = be -> new DefaultContainerProvider<CraftingManagerContainer>("Crafting Manager")
            .containerSupplier((windowId, player) -> new CraftingManagerContainer(windowId, be.getBlockPos(), be, player))
            .itemHandler(() -> be.items);

    // @todo save/load requests in NBT
    private final CraftingQueue[] queues = new CraftingQueue[4];
    private boolean devicesDirty = true;

    public CraftingManagerTileEntity(BlockPos pos, BlockState state) {
        super(CraftingManagerModule.CRAFTING_MANAGER.be().get(), pos, state);
        for (int i = 0; i < 4; i++) {
            queues[i] = new CraftingQueue();
        }
    }

    private Optional<ICraftingDevice> getDevice(int queueIndex) {
        if (devicesDirty) {
            updateDevices();
        }
        return Optional.ofNullable(queues[queueIndex].getDevice());
    }

    public GenericItemHandler getItems() {
        return items;
    }

    /**
     * Tick is called manually from the crafting system.
     * It will return true if one of the crafters finished a craft
     */
    public boolean tick(CraftingSystem system) {
        boolean rc = false;
        for (int queueIndex = 0; queueIndex < 4; queueIndex++) {
            int finalQueueIndex = queueIndex;
            rc = getDevice(queueIndex).map(device -> {
                device.tick();
                if (device.getStatus() == ICraftingDevice.Status.READY) {
                    sendResultsBack(finalQueueIndex, system);
                    return true;
                }
                return false;
            }).orElse(rc);
        }
        return rc;
    }

    private void sendResultsBack(int queueIndex, CraftingSystem system) {
        List<ItemStack> output = getDevice(queueIndex).map(device -> device.extractOutput(level)).orElse(Collections.emptyList());
        StorageScannerTileEntity storage = system.getStorage();
        for (ItemStack stack : output) {
            ItemStack left = storage.insertInternal(stack, false);
            // @todo What should we do here? Currently we just spawn the items in the world
            Containers.dropItemStack(level, storage.getBlockPos().getX() + .5, storage.getBlockPos().getY() + 1.5, storage.getBlockPos().getZ() + .5, left);
        }
    }

    public boolean canCraft(Ingredient ingredient) {
        for (int i = 4; i < items.getSlots(); i++) {
            ItemStack card = items.getStackInSlot(i);
            if (!card.isEmpty()) {
                ItemStack result = CraftingCardItem.getResult(card);
                if (ingredient.test(result)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<ItemStack> getCraftables() {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 4; i < items.getSlots(); i++) {
            ItemStack card = items.getStackInSlot(i);
            if (!card.isEmpty()) {
                ItemStack result = CraftingCardItem.getResult(card);
                if (!result.isEmpty()) {
                    stacks.add(result);
                }
            }
        }
        return stacks;
    }

    public static final double QUALITY_NOTPOSSIBLE = -1;
    public static final double QUALITY_DEVICEIDLE = 10000;   // If greater or equal then this number the craft can start immediatelly

    /**
     * Return a quality number that indicates how good this crafting manager is for crafting the
     * requested item. Higher numbers are better. A negative number means that this crafting manager
     * cannot craft this at all
     *
     */
    public Pair<Double, Integer> getCraftingQuality(Ingredient ingredient, int amount) {
        AtomicDouble bestQuality = new AtomicDouble(-1);
        AtomicInteger bestDevice = new AtomicInteger(-1);
        for (int queueIndex = 0; queueIndex < 4; queueIndex++) {
            int finalQueueIndex = queueIndex;
            getDevice(queueIndex).ifPresent(device -> {
                Queue<CraftingRequest> requests = queues[finalQueueIndex].getRequests();
                double baseQuality = Math.max(0.0, 1.0 - (requests.size() / 10.0));   // Amount of requests negatively impacts the quality
                switch (device.getStatus()) {
                    case IDLE -> baseQuality += QUALITY_DEVICEIDLE;
                    case READY -> baseQuality += 0.5;
                    case BUSY -> { }
                }
                double quality = -1;
                for (int i = getFirstCardIndex(finalQueueIndex); i < getLastCardIndex(finalQueueIndex); i++) {
                    ItemStack card = items.getStackInSlot(i);
                    if (!card.isEmpty()) {
                        ItemStack result = CraftingCardItem.getResult(card);
                        if (ingredient.test(result)) {
                            quality = baseQuality;
                            break;
                        }
                    }
                }
                if (quality >= 0 && quality > bestQuality.get()) {
                    bestQuality.set(quality);
                    bestDevice.set(finalQueueIndex);
                }
            });
        }
        return Pair.of(bestQuality.get(), bestDevice.get());
    }

    private int getLastCardIndex(int queueIndex) {
        return 4 + queueIndex * 8 + 8;
    }

    private int getFirstCardIndex(int queueIndex) {
        return 4 + queueIndex * 8;
    }

    public CraftingQueue[] getQueues() {
        return queues;
    }

    /**
     * Get the list of ingredients for a given request
     */
    @Nonnull
    public List<Optional<Ingredient>> getIngredients(int queueIndex, CraftingRequest request) {
        CraftingQueue queue = queues[queueIndex];
        if (devicesDirty) {
            updateDevices();
        }
        for (int i = getFirstCardIndex(queueIndex) ; i < getLastCardIndex(queueIndex) ; i++) {
            ItemStack cardStack = items.getStackInSlot(i);
            if (!cardStack.isEmpty()) {
                ItemStack cardResult = CraftingCardItem.getResult(cardStack);
                if (request.ingredient().test(cardResult)) {
                    // Request needed ingredients from the storage scanner
                    queue.getDevice().setupCraft(level, cardStack);
                    return queue.getDevice().getIngredients();
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Actually start the craft on the given device. The given ingredients are already
     * extracted from the storage scanner and are supposed to be consumed by the device.
     * We know the given device is idle so it should be able to start immediatelly
     *
     * If for some obscure reason the craft still fails this function returns false
     */
    public boolean startCraft(int queueIndex, CraftingRequest request, List<ItemStack> ingredients) {
        CraftingQueue queue = queues[queueIndex];
        if (devicesDirty) {
            updateDevices();
        }

        if (!queue.getDevice().insertIngredients(level, ingredients)) {
            // For some reason there was a failure inserting ingredients
            return false;
        }

        return true;
    }

    private void updateDevices() {
        for (int i = 0; i < 4; i++) {
// @todo THIS IS WRONG! Should not remove devices that are already present because they may be doing something!
            ItemStack stack = items.getStackInSlot(i);
            Identifier id = Tools.getId(stack);
            Identifier deviceId = CraftingManagerModule.CRAFTING_DEVICE_REGISTRY.getDeviceForBlock(id);
            if (deviceId != null) {
                Supplier<ICraftingDevice> device = CraftingManagerModule.CRAFTING_DEVICE_REGISTRY.getDeviceSupplier(deviceId);
                ICraftingDevice craftingDevice = device.get();
                queues[i].setDevice(craftingDevice);
                // Init device from ID?
            }
        }
        devicesDirty = false;
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        int i = 0;
        for (CompoundTag deviceTag : input.listOrEmpty("devices", CompoundTag.CODEC)) {
            if (i >= queues.length) {
                break;
            }
            Identifier deviceId = Identifier.tryParse(deviceTag.getStringOr("deviceId", ""));
            if (!deviceTag.isEmpty() && deviceId != null) {
                Supplier<ICraftingDevice> supplier = CraftingManagerModule.CRAFTING_DEVICE_REGISTRY.getDeviceSupplier(deviceId);
                if (supplier != null) {
                    ICraftingDevice device = supplier.get();
                    queues[i].setDevice(device);
                    device.read(input.lookup(), deviceTag);
                }
            }
            i++;
        }
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        var devices = output.list("devices", CompoundTag.CODEC);
        for (CraftingQueue queue : queues) {
            CompoundTag deviceTag = new CompoundTag();
            if (queue.hasDevice()) {
                queue.getDevice().write(level.registryAccess(), deviceTag);
                deviceTag.putString("deviceId", queue.getDevice().getID().toString());
            }
            devices.add(deviceTag);
        }
    }
}
