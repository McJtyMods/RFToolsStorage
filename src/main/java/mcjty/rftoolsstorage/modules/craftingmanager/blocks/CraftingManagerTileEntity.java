package mcjty.rftoolsstorage.modules.craftingmanager.blocks;

import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerSetup;
import mcjty.rftoolsstorage.modules.craftingmanager.CraftingRequest;
import mcjty.rftoolsstorage.modules.craftingmanager.ICraftingDevice;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
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
            .containerSupplier((windowId,player) -> new CraftingManagerContainer(windowId, getPos(), player, CraftingManagerTileEntity.this))
            .itemHandler(() -> getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(h -> h).orElseThrow(RuntimeException::new)));

    private ICraftingDevice[] devices = new ICraftingDevice[4];
    private Queue<CraftingRequest> requests = new ArrayDeque<>();

    public CraftingManagerTileEntity() {
        super(CraftingManagerSetup.TYPE_CRAFTING_MANAGER);
        for (int i = 0 ; i < 4 ; i++) {
            devices[i] = null;
        }
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            itemHandler.ifPresent(h -> {
                for (int i = 0 ; i < 4 ; i++) {
                    if (devices[i] != null) {
                        devices[i].tick();
                    }
                }

                CraftingRequest request = requests.peek();
                if (request != null) {
                    if (fireRequest(request)) {
                        requests.remove();
                    }
                }
            });
        }
    }

    private boolean fireRequest(CraftingRequest request) {
        for (ICraftingDevice device : devices) {
            if (device.getStatus() == ICraftingDevice.Status.IDLE) {
                return true;
            }
        }

        return false;
    }

    private void updateDevices() {
        itemHandler.ifPresent(h -> {
            for (int i = 0 ; i < 4 ; i++) {
                ItemStack deviceStack = h.getStackInSlot(i);
                ResourceLocation id = deviceStack.getItem().getRegistryName();
                ICraftingDevice device = RFToolsStorage.setup.craftingDeviceRegistry.get(id);
                devices[i] = device;
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
