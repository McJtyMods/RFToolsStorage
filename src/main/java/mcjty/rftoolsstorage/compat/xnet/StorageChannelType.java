package mcjty.rftoolsstorage.compat.xnet;

import com.mojang.serialization.MapCodec;
import mcjty.rftoolsbase.api.storage.IStorageScanner;
import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StorageChannelType implements IChannelType {

    @Override
    public String getID() {
        return "rftools.storage";
    }

    @Override
    public String getName() {
        return "Storage";
    }

    @Override
    public MapCodec<? extends IChannelSettings> getCodec() {
        return StorageChannelSettings.CODEC;
    }

    @Override
    public MapCodec<? extends IConnectorSettings> getConnectorCodec() {
        return StorageConnectorSettings.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ? extends IChannelSettings> getStreamCodec() {
        return StorageChannelSettings.STREAM_CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ? extends IConnectorSettings> getConnectorStreamCodec() {
        return StorageConnectorSettings.STREAM_CODEC;
    }

    @Override
    public boolean supportsBlock(@Nonnull Level world, @Nonnull BlockPos pos, @Nullable Direction side) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te == null) {
            return false;
        }
        IItemHandler h = world.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        if (h != null && h.getSlots() > 0) {
            return true;
        }
        if (te instanceof Container) {
            return true;
        }
        if (te instanceof IStorageScanner) {
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public IConnectorSettings createConnector(@Nonnull Direction side) {
        return new StorageConnectorSettings(side);
    }

    @Nonnull
    @Override
    public IChannelSettings createChannel() {
        return new StorageChannelSettings();
    }
}
