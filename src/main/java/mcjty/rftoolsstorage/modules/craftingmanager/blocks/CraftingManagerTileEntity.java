package mcjty.rftoolsstorage.modules.craftingmanager.blocks;

import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolsbase.modules.crafting.items.CraftingCardItem;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerSetup;
import mcjty.rftoolsstorage.modules.craftingmanager.system.CraftingQueue;
import mcjty.rftoolsstorage.modules.craftingmanager.system.CraftingRequest;
import mcjty.rftoolsstorage.modules.craftingmanager.system.CraftingSystem;
import mcjty.rftoolsstorage.modules.craftingmanager.system.ICraftingDevice;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class CraftingManagerTileEntity extends GenericTileEntity {

    public static final ModelProperty<BlockState> MIMIC[] = new ModelProperty[]{
            new ModelProperty<>(),
            new ModelProperty<>(),
            new ModelProperty<>(),
            new ModelProperty<>()
    };

    private LazyOptional<IItemHandler> itemHandler = LazyOptional.of(this::createItemHandler);
    private LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<CraftingManagerContainer>("Modular Storage")
            .containerSupplier((windowId, player) -> new CraftingManagerContainer(windowId, getPos(), player, CraftingManagerTileEntity.this))
            .itemHandler(() -> getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(h -> h).orElseThrow(RuntimeException::new)));

    // @todo save/load requests in NBT
    private CraftingQueue[] queues = new CraftingQueue[4];

    public CraftingManagerTileEntity() {
        super(CraftingManagerSetup.TYPE_CRAFTING_MANAGER.get());
        for (int i = 0; i < 4; i++) {
            queues[i] = new CraftingQueue();
        }
    }

    /**
     * Tick is called manually from the crafting system.
     * It will return true if one of the crafters finished a craft
     */
    public boolean tick(CraftingSystem system) {
        return itemHandler.map(h -> {
            boolean rc = false;
            for (int queueIndex = 0; queueIndex < 4; queueIndex++) {
                if (queues[queueIndex].hasDevice()) {
                    ICraftingDevice device = queues[queueIndex].getDevice();
                    device.tick();
                    if (device.getStatus() == ICraftingDevice.Status.READY) {
                        sendResultsBack(queueIndex, system);
                        rc = true;
                    }
                }
            }
            return rc;
        }).orElse(false);
    }

    private void sendResultsBack(int queueIndex, CraftingSystem system) {
        List<ItemStack> output = queues[queueIndex].getDevice().extractOutput();
        StorageScannerTileEntity storage = system.getStorage();
        for (ItemStack stack : output) {
            ItemStack left = storage.insertInternal(stack, false);
            // @todo What should we do here? Currently we just spawn the items in the world
            InventoryHelper.spawnItemStack(world, storage.getPos().getX() + .5, storage.getPos().getY() + 1.5, storage.getPos().getZ() + .5, left);
        }
    }

    public boolean canCraft(Ingredient ingredient) {
        return itemHandler.map(h -> {
            for (int i = 4; i < h.getSlots(); i++) {
                ItemStack card = h.getStackInSlot(i);
                if (!card.isEmpty()) {
                    ItemStack result = CraftingCardItem.getResult(card);
                    if (ingredient.test(result)) {
                        return true;
                    }
                }
            }
            return false;
        }).orElse(false);
    }

    public List<ItemStack> getCraftables() {
        List<ItemStack> stacks = new ArrayList<>();
        itemHandler.ifPresent(h -> {
            for (int i = 4; i < h.getSlots(); i++) {
                ItemStack card = h.getStackInSlot(i);
                if (!card.isEmpty()) {
                    ItemStack result = CraftingCardItem.getResult(card);
                    if (!result.isEmpty()) {
                        stacks.add(result);
                    }
                }
            }
        });
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
        return itemHandler.map(h -> {
            double bestQuality = -1;
            int bestDevice = -1;
            for (int queueIndex = 0; queueIndex < 4; queueIndex++) {
                if (queues[queueIndex].hasDevice()) {
                    ICraftingDevice device = queues[queueIndex].getDevice();
                    Queue<CraftingRequest> requests = queues[queueIndex].getRequests();
                    double baseQuality = Math.max(0.0, 1.0 - (requests.size() / 10.0));   // Amount of requests negatively impacts the quality
                    switch (device.getStatus()) {
                        case IDLE:
                            baseQuality += QUALITY_DEVICEIDLE;
                            break;
                        case READY:
                            baseQuality += 0.5;
                            break;
                        case BUSY:
                        default:
                            break;
                    }
                    double quality = -1;
                    for (int i = getFirstCardIndex(queueIndex); i < getLastCardIndex(queueIndex); i++) {
                        ItemStack card = h.getStackInSlot(queueIndex);
                        if (!card.isEmpty()) {
                            ItemStack result = CraftingCardItem.getResult(card);
                            if (ingredient.test(result)) {
                                quality = baseQuality;
                                break;
                            }
                        }
                    }
                    if (quality >= 0 && quality > bestQuality) {
                        bestQuality = quality;
                        bestDevice = queueIndex;
                    }
                }
            }
            return Pair.of(bestQuality, bestDevice);
        }).orElse(Pair.of(-1.0, -1));
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
    public List<Ingredient> getIngredients(int queueIndex, CraftingRequest request) {
        return itemHandler.map(h -> {
            CraftingQueue queue = queues[queueIndex];
            for (int i = getFirstCardIndex(queueIndex) ; i < getLastCardIndex(queueIndex) ; i++) {
                ItemStack cardStack = h.getStackInSlot(i);
                if (!cardStack.isEmpty()) {
                    ItemStack cardResult = CraftingCardItem.getResult(cardStack);
                    if (request.getIngredient().test(cardResult)) {
                        // Request needed ingredients from the storage scanner
                        IRecipe recipe = CraftingCardItem.findRecipe(world, cardStack, queue.getDevice().getRecipeType());
                        List<Ingredient> ingredients;
                        if (recipe != null) {
                            ingredients = recipe.getIngredients();
                        } else {
                            ingredients = CraftingCardItem.getIngredients(cardStack);
                        }
                        return ingredients;
                    }
                }
            }
            return Collections.<Ingredient>emptyList();
        }).orElse(Collections.<Ingredient>emptyList());
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
        if (!queue.getDevice().insertIngredients(ingredients, world)) {
            // For some reason there was a failure inserting ingredients
            return false;
        }

        return true;
    }

    private void updateDevices() {
        itemHandler.ifPresent(h -> {
            for (int i = 0; i < 4; i++) {
                ItemStack deviceStack = h.getStackInSlot(i);
                ResourceLocation id = deviceStack.getItem().getRegistryName();
                ICraftingDevice device = RFToolsStorage.setup.craftingDeviceRegistry.get(id);
                queues[i].setDevice(device);
            }
        });
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        itemHandler.ifPresent(h -> {
            ItemStack origMimic0 = h.getStackInSlot(0);
            ItemStack origMimic1 = h.getStackInSlot(1);
            ItemStack origMimic2 = h.getStackInSlot(2);
            ItemStack origMimic3 = h.getStackInSlot(3);
            readClientDataFromNBT(pkt.getNbtCompound());
            ItemStack mimic0 = h.getStackInSlot(0);
            ItemStack mimic1 = h.getStackInSlot(1);
            ItemStack mimic2 = h.getStackInSlot(2);
            ItemStack mimic3 = h.getStackInSlot(3);
            if (!ItemStack.areItemsEqual(origMimic0, mimic0) || !ItemStack.areItemsEqual(origMimic1, mimic1) || !ItemStack.areItemsEqual(origMimic2, mimic2) || !ItemStack.areItemsEqual(origMimic3, mimic3)) {
                ModelDataManager.requestModelDataRefresh(this);
                world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
            }
        });
    }


    @Nonnull
    @Override
    public IModelData getModelData() {
        return itemHandler.map(h -> {
            BlockState mimic0 = h.getStackInSlot(0).isEmpty() ? null : ((BlockItem) h.getStackInSlot(0).getItem()).getBlock().getDefaultState();
            BlockState mimic1 = h.getStackInSlot(1).isEmpty() ? null : ((BlockItem) h.getStackInSlot(1).getItem()).getBlock().getDefaultState();
            BlockState mimic2 = h.getStackInSlot(2).isEmpty() ? null : ((BlockItem) h.getStackInSlot(2).getItem()).getBlock().getDefaultState();
            BlockState mimic3 = h.getStackInSlot(3).isEmpty() ? null : ((BlockItem) h.getStackInSlot(3).getItem()).getBlock().getDefaultState();

            return new ModelDataMap.Builder()
                    .withInitial(MIMIC[0], mimic0)
                    .withInitial(MIMIC[1], mimic1)
                    .withInitial(MIMIC[2], mimic2)
                    .withInitial(MIMIC[3], mimic3)
                    .build();
        }).orElseThrow(IllegalStateException::new);
    }


    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        return tagCompound;
    }

    @Nonnull
    private NoDirectionItemHander createItemHandler() {
        return new NoDirectionItemHander(this, CraftingManagerContainer.CONTAINER_FACTORY) {

            @Override
            protected void onUpdate(int index) {
                if (index < 4) {
                    markDirtyClient();
                    updateDevices();
                }
                super.onUpdate(index);
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandler.cast();
        }
        if (cap == CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY) {
            return screenHandler.cast();
        }
        return super.getCapability(cap, facing);
    }
}
