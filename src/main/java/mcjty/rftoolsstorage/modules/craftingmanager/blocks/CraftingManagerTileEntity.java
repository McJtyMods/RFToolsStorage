package mcjty.rftoolsstorage.modules.craftingmanager.blocks;

import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerSetup;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CraftingManagerTileEntity extends GenericTileEntity implements ITickableTileEntity {

    private LazyOptional<IItemHandler> itemHandler = LazyOptional.of(this::createItemHandler);
    private LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<CraftingManagerContainer>("Modular Storage")
            .containerSupplier((windowId,player) -> new CraftingManagerContainer(windowId, getPos(), player, CraftingManagerTileEntity.this))
            .itemHandler(() -> getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(h -> h).orElseThrow(RuntimeException::new)));

    public CraftingManagerTileEntity() {
        super(CraftingManagerSetup.TYPE_CRAFTING_MANAGER);
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
        }
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
        return new NoDirectionItemHander(this, CraftingManagerContainer.CONTAINER_FACTORY);
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
