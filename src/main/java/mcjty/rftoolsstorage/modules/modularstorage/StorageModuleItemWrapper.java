package mcjty.rftoolsstorage.modules.modularstorage;

import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.storage.StorageHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class StorageModuleItemWrapper implements ICapabilityProvider {

    private final LazyOptional<IItemHandler> holder = LazyOptional.of(this::createHandler);

    private final ItemStack itemStack;
    private final UUID uuid;
    private ItemStackHandler handler;

    public StorageModuleItemWrapper(ItemStack stack, UUID uuid) {
        this.itemStack = stack;
        this.uuid = uuid;
    }

    private ItemStackHandler createHandler() {
        if (handler == null) {
            if (EffectiveSide.get() == LogicalSide.SERVER) {
                return StorageHolder.get().getStorageEntry(uuid).getHandler();
            } else {
                return RFToolsStorage.setup.clientStorageHolder.getStorage(uuid, 0/*@todo*/);
            }
        }
        return handler;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return holder.cast();
    }
}
