package mcjty.rftoolsstorage.compat.xnet;

import mcjty.lib.varia.WorldTools;
import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import mcjty.rftoolsbase.api.xnet.channels.IControllerContext;
import mcjty.rftoolsbase.api.xnet.gui.IEditorGui;
import mcjty.rftoolsbase.api.xnet.gui.IndicatorIcon;
import mcjty.rftoolsbase.api.xnet.helper.DefaultChannelSettings;
import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import mcjty.rftoolsstorage.modules.scanner.tools.InventoryAccessSettings;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageChannelSettings extends DefaultChannelSettings implements IChannelSettings {

    public static final ResourceLocation iconGuiElements = new ResourceLocation(RFToolsStorage.MODID, "textures/gui/guielements.png");

    private List<Pair<SidedConsumer, StorageConnectorSettings>> storageControllers = null;
    private Map<BlockPos, InventoryAccessSettings> access = null;

    private int delay = 0;

    @Override
    public void readFromNBT(CompoundNBT tag) {

    }

    @Override
    public void writeToNBT(CompoundNBT tag) {

    }

    @Override
    public void tick(int channel, IControllerContext context) {
        if (updateCache(channel, context)) {
            delay = 0; // If cache was updated we send new state immediatelly
        }

        delay--;
        if (delay > 0) {
            return;
        }
        delay = 10;

        World world = context.getControllerWorld();
        for (Pair<SidedConsumer, StorageConnectorSettings> entry : storageControllers) {
            BlockPos extractorPos = context.findConsumerPosition(entry.getKey().getConsumerId());
            if (extractorPos != null) {
                Direction side = entry.getKey().getSide();
                BlockPos pos = extractorPos.relative(side);
                if (!WorldTools.isLoaded(world, pos)) {
                    continue;
                }

                TileEntity te = world.getBlockEntity(pos);
                if (te instanceof StorageScannerTileEntity) {
                    StorageScannerTileEntity scanner = (StorageScannerTileEntity) te;
                    scanner.register(access);
                }
            }
        }
    }

    private BlockPos getInventory(IControllerContext context, SidedConsumer sidedConsumer) {
        BlockPos consumerPos = context.findConsumerPosition(sidedConsumer.getConsumerId());
        if (consumerPos != null) {
            Direction side = sidedConsumer.getSide();
            BlockPos pos = consumerPos.relative(side);
            TileEntity te = context.getControllerWorld().getBlockEntity(pos);
            if (te != null) {
                LazyOptional<IItemHandler> handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                if (handler.isPresent()) {
                    return pos;
                }
            }
        }
        return null;
    }

    private boolean updateCache(int channel, IControllerContext context) {
        if (storageControllers == null) {
            storageControllers = new ArrayList<>();
            access = new HashMap<>();

            Map<SidedConsumer, IConnectorSettings> connectors = context.getConnectors(channel);
            for (Map.Entry<SidedConsumer, IConnectorSettings> entry : connectors.entrySet()) {
                StorageConnectorSettings con = (StorageConnectorSettings) entry.getValue();
                addInventory(context, entry, con);
            }
            connectors = context.getRoutedConnectors(channel);
            for (Map.Entry<SidedConsumer, IConnectorSettings> entry : connectors.entrySet()) {
                StorageConnectorSettings con = (StorageConnectorSettings) entry.getValue();
                addInventory(context, entry, con);
            }
            return true;
        }
        return false;
    }

    private void addInventory(IControllerContext context, Map.Entry<SidedConsumer, IConnectorSettings> entry, StorageConnectorSettings con) {
        if (con.getMode() == StorageConnectorSettings.Mode.STORAGE) {
            storageControllers.add(Pair.of(entry.getKey(), con));
        } else {
            BlockPos inventory = getInventory(context, entry.getKey());
            if (inventory != null) {
                access.put(inventory, ((StorageConnectorSettings)entry.getValue()).getAccessSettings());
            }
        }
    }


    @Override
    public void cleanCache() {
        storageControllers = null;
        access = null;
    }

    @Override
    public int getColors() {
        return 0;
    }

    @Nullable
    @Override
    public IndicatorIcon getIndicatorIcon() {
        return new IndicatorIcon(iconGuiElements, 0, 57, 11, 10);
    }

    @Nullable
    @Override
    public String getIndicator() {
        return null;
    }

    @Override
    public boolean isEnabled(String tag) {
        return true;
    }

    @Override
    public void createGui(IEditorGui gui) {

    }

    @Override
    public void update(Map<String, Object> data) {

    }
}
