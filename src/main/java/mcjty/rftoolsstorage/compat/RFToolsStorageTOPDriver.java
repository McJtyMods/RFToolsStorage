package mcjty.rftoolsstorage.compat;

import mcjty.lib.compat.theoneprobe.McJtyLibTOPDriver;
import mcjty.lib.compat.theoneprobe.TOPDriver;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageModule;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageTileEntity;
import mcjty.theoneprobe.api.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

import java.util.HashMap;
import java.util.Map;

import static mcjty.theoneprobe.api.TextStyleClass.INFO;
import static mcjty.theoneprobe.api.TextStyleClass.WARNING;

public class RFToolsStorageTOPDriver implements TOPDriver {

    public static final RFToolsStorageTOPDriver DRIVER = new RFToolsStorageTOPDriver();

    private final Map<ResourceLocation, TOPDriver> drivers = new HashMap<>();

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
        ResourceLocation id = Tools.getId(blockState);
        if (!drivers.containsKey(id)) {
            if (blockState.getBlock() == ModularStorageModule.MODULAR_STORAGE.get()) {
                drivers.put(id, new ModularStorageDriver());
            } else {
                drivers.put(id, new DefaultDriver());
            }
        }
        TOPDriver driver = drivers.get(id);
        if (driver != null) {
            driver.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        }
    }

    private static class DefaultDriver implements TOPDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
            McJtyLibTOPDriver.DRIVER.addStandardProbeInfo(mode, probeInfo, player, world, blockState, data);
        }
    }

    private static class ModularStorageDriver implements TOPDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
            McJtyLibTOPDriver.DRIVER.addStandardProbeInfo(mode, probeInfo, player, world, blockState, data);
            Tools.safeConsume(world.getBlockEntity(data.getPos()), (ModularStorageTileEntity te) -> {
                int maxSize = te.getMaxSize();
                if (maxSize == 0) {
                    probeInfo.text(CompoundText.create().style(WARNING).text("No storage module!"));
                } else {
                    IItemHandler cardHandler = te.getCardHandler();
                    ItemStack storageModule = cardHandler.getStackInSlot(ModularStorageContainer.SLOT_STORAGE_MODULE);
                    if (!storageModule.isEmpty() && storageModule.getTag().contains("display")) {
                        probeInfo.text(CompoundText.createLabelInfo("Module: ", storageModule.getHoverName()));
                    }
                    int stacks = te.getNumStacks();
                    if (stacks == -1) {
                        probeInfo.text(CompoundText.createLabelInfo("Maximum size: ", maxSize));
                    } else {
                        probeInfo.text(CompoundText.create().style(INFO).text(stacks + " out of " + maxSize));
                    }
                }
            }, "Bad tile entity!");
        }
    }

}
