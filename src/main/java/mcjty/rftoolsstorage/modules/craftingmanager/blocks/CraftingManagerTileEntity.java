package mcjty.rftoolsstorage.modules.craftingmanager.blocks;

import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolsbase.modules.crafting.items.CraftingCardItem;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerSetup;
import mcjty.rftoolsstorage.modules.craftingmanager.tools.CraftingQueue;
import mcjty.rftoolsstorage.modules.craftingmanager.tools.CraftingRequest;
import mcjty.rftoolsstorage.modules.craftingmanager.tools.ICraftingDevice;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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
import java.util.List;
import java.util.Queue;

public class CraftingManagerTileEntity extends GenericTileEntity implements ITickableTileEntity {

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
        super(CraftingManagerSetup.TYPE_CRAFTING_MANAGER);
        for (int i = 0; i < 4; i++) {
            queues[i] = new CraftingQueue();
        }
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            itemHandler.ifPresent(h -> {
                for (int queueIndex = 0; queueIndex < 4; queueIndex++) {
                    if (queues[queueIndex].hasDevice()) {
                        ICraftingDevice device = queues[queueIndex].getDevice();
                        device.tick();
                        if (device.getStatus() == ICraftingDevice.Status.READY) {
                            sendResultsBack(queueIndex);
                        }
                        if (device.getStatus() == ICraftingDevice.Status.IDLE) {
                            Queue<CraftingRequest> requests = queues[queueIndex].getRequests();
                            CraftingRequest request = requests.peek();
                            if (request != null) {
                                if (fireRequest(queueIndex, request)) {
                                    requests.remove();
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    private void sendResultsBack(int queueIndex) {
        // @todo
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

    /**
     * Return a quality number that indicates how good this crafting manager is for crafting the
     * requested item. Higher numbers are better. A negative number means that this crafting manager
     * cannot craft this at all
     */
    public Pair<Double, Integer> getCraftingQuality(ItemStack stack, int amount) {
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
                            baseQuality += 1;
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
                            if (InventoryHelper.isItemStackConsideredEqual(result, stack)) {
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

    public void request(ItemStack requested, int amount, BlockPos requester, int queueIndex) {
        queues[queueIndex].getRequests().add(new CraftingRequest(requested, amount, requester));
    }

    private boolean fireRequest(int queueIndex, CraftingRequest request) {
        return itemHandler.map(h -> {
            CraftingQueue queue = queues[queueIndex];
            for (int i = getFirstCardIndex(queueIndex) ; i < getLastCardIndex(queueIndex) ; i++) {
                ItemStack cardStack = h.getStackInSlot(i);
                if (!cardStack.isEmpty()) {
                    ItemStack cardResult = CraftingCardItem.getResult(cardStack);
                    if (InventoryHelper.isItemStackConsideredEqual(request.getStack(), cardResult)) {
                        // Request needed ingredients from the storage scanner
                        IRecipe recipe = CraftingCardItem.findRecipe(world, cardStack, queue.getDevice().getRecipeType());
                        List<Ingredient> ingredients;
                        if (recipe != null) {
                            ingredients = recipe.getIngredients();
                        } else {
                            ingredients = CraftingCardItem.getIngredients(cardStack);
                        }
                        TileEntity te = world.getTileEntity(request.getRequester());
                        if (te instanceof StorageScannerTileEntity) {
                            StorageScannerTileEntity scanner = (StorageScannerTileEntity) te;
                            List<ItemStack> stacks = scanner.requestIngredients(ingredients, pos);
                            if (stacks == null) {
                                // Craft is not possible
                                return false;
                            } else {
                                // @todo
                            }
                        }
                        return true;
                    }
                }
            }
            return false;
        }).orElse(false);
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
