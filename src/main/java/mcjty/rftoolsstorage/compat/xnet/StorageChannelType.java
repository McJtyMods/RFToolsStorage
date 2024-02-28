package mcjty.rftoolsstorage.compat.xnet;

import mcjty.rftoolsbase.api.storage.IStorageScanner;
import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.capabilities.ForgeCapabilities;

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
    public boolean supportsBlock(@Nonnull Level world, @Nonnull BlockPos pos, @Nullable Direction side) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te == null) {
            return false;
        }
        if (te.getCapability(ForgeCapabilities.ITEM_HANDLER, side).isPresent()) {
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
