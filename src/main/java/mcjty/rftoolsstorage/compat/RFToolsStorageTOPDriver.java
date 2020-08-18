package mcjty.rftoolsstorage.compat;

import mcjty.lib.compat.theoneprobe.McJtyLibTOPDriver;
import mcjty.lib.compat.theoneprobe.TOPDriver;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageSetup;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageContainer;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageTileEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import java.util.HashMap;
import java.util.Map;

public class RFToolsStorageTOPDriver implements TOPDriver {

    public static final RFToolsStorageTOPDriver DRIVER = new RFToolsStorageTOPDriver();

    private final Map<ResourceLocation, TOPDriver> drivers = new HashMap<>();

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        ResourceLocation id = blockState.getBlock().getRegistryName();
        if (!drivers.containsKey(id)) {
            if (blockState.getBlock() == ModularStorageSetup.MODULAR_STORAGE.get()) {
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
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            McJtyLibTOPDriver.DRIVER.addStandardProbeInfo(mode, probeInfo, player, world, blockState, data);
        }
    }

    private static class ModularStorageDriver implements TOPDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            McJtyLibTOPDriver.DRIVER.addStandardProbeInfo(mode, probeInfo, player, world, blockState, data);
            Tools.safeConsume(world.getTileEntity(data.getPos()), (ModularStorageTileEntity te) -> {
                int maxSize = te.getMaxSize();
                if (maxSize == 0) {
                    probeInfo.text(new StringTextComponent(TextFormatting.YELLOW + "No storage module!"));  // @todo 1.16
                } else {
                    IItemHandler cardHandler = te.getCardHandler();
                    ItemStack storageModule = cardHandler.getStackInSlot(ModularStorageContainer.SLOT_STORAGE_MODULE);
                    if (!storageModule.isEmpty() && storageModule.getTag().contains("display")) {
                        probeInfo.text(new StringTextComponent(TextFormatting.YELLOW + "Module: " + TextFormatting.WHITE + storageModule.getDisplayName()));    // @todo 1.16
                    }
                    int stacks = te.getNumStacks();
                    if (stacks == -1) {
                        probeInfo.text(new StringTextComponent(TextFormatting.YELLOW + "Maximum size: " + maxSize));    // @todo 1.16
                    } else {
                        probeInfo.text(new StringTextComponent(TextFormatting.GREEN + "" + stacks + " out of " + maxSize)); // @todo 1.16
                    }
                }
            }, "Bad tile entity!");
        }
    }

}
