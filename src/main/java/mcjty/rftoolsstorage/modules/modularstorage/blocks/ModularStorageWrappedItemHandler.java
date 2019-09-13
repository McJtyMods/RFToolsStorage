package mcjty.rftoolsstorage.modules.modularstorage.blocks;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolsstorage.modules.modularstorage.items.StorageModuleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class ModularStorageWrappedItemHandler extends NoDirectionItemHander {

    private Optional<IItemHandler> wrapped = Optional.empty();

    public ModularStorageWrappedItemHandler(GenericTileEntity te, ContainerFactory factory) {
        super(te, factory);
    }

    public void setWrapped(@Nullable IItemHandler handler) {
        // @todo need caching because the wrapped itemhandler will be an item that stores itemstacks in the NBT
        // and that's slow to manipulate
        wrapped = Optional.ofNullable(handler);
    }

    @Override
    public int getSlots() {
        return super.getSlots() + wrapped.map(IItemHandler::getSlots).orElse(0);
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < ModularStorageContainer.SLOT_STORAGE) {
            return super.getStackInSlot(slot);
        } else {
            return wrapped.map(h -> h.getStackInSlot(slot - ModularStorageContainer.SLOT_STORAGE)).orElse(ItemStack.EMPTY);
        }
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (slot < ModularStorageContainer.SLOT_STORAGE) {
            return super.insertItem(slot, stack, simulate);
        } else {
            return wrapped.map(h -> h.insertItem(slot - ModularStorageContainer.SLOT_STORAGE, stack, simulate)).orElse(ItemStack.EMPTY);
        }
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot < ModularStorageContainer.SLOT_STORAGE) {
            return super.extractItem(slot, amount, simulate);
        } else {
            return wrapped.map(h -> h.extractItem(slot - ModularStorageContainer.SLOT_STORAGE, amount, simulate)).orElse(ItemStack.EMPTY);
        }
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (slot < ModularStorageContainer.SLOT_STORAGE) {
            super.setStackInSlot(slot, stack);
        } else {
            wrapped.filter(h -> h instanceof IItemHandlerModifiable).ifPresent(h -> ((IItemHandlerModifiable)h).setStackInSlot(slot - ModularStorageContainer.SLOT_STORAGE, stack));
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot < ModularStorageContainer.SLOT_STORAGE) {
            return super.getSlotLimit(slot);
        } else {
            return wrapped.map(h -> h.getSlotLimit(slot - ModularStorageContainer.SLOT_STORAGE)).orElse(0);
        }
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (slot < ModularStorageContainer.SLOT_STORAGE) {
            switch (slot) {
                case ModularStorageContainer.SLOT_STORAGE_MODULE:
                    // @todo 1.14 allow all kinds of containers
                    return !stack.isEmpty() && stack.getItem() instanceof StorageModuleItem;
                    // @todo 1.14
//                case ModularStorageContainer.SLOT_FILTER_MODULE:
//                    return !stack.isEmpty() && stack.getItem() instanceof StorageFilterItem;
//                case ModularStorageContainer.SLOT_TYPE_MODULE:
//                    return !stack.isEmpty() && stack.getItem() instanceof StorageTypeItem;
            }
            return super.isItemValid(slot, stack);
        } else {
            return wrapped.map(h -> h.isItemValid(slot - ModularStorageContainer.SLOT_STORAGE, stack)).orElse(false);
        }
    }

    @Override
    public ListNBT serializeNBT() {
        // We only serialize our own three slots. Not the ones from the storage item as that item handles it itself
        return super.serializeNBT();
    }

    @Override
    public void deserializeNBT(ListNBT nbt) {
        // We only deserialize our own three slots. Not the ones from the storage item as that item handles it itself
        super.deserializeNBT(nbt);
    }
}
