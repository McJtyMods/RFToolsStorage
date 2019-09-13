package mcjty.rftoolsstorage.modules.modularstorage;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
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
            handler = new ItemStackHandler(100) {
                @Override
                protected void onContentsChanged(int slot) {
                    CompoundNBT nbt = serializeNBT();
                    itemStack.getOrCreateTag().put("Items", nbt);
                }
            };
            CompoundNBT items = itemStack.getOrCreateTag().getCompound("Items");
            handler.deserializeNBT(items);
        }
        return handler;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return holder.cast();
    }
}
