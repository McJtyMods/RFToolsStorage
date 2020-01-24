package mcjty.rftoolsstorage.modules.scanner.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbase.api.infoscreen.IInformationScreenInfo;
import mcjty.rftoolsbase.modules.informationscreen.client.DefaultPowerInformationRenderer;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerTileEntity;
import mcjty.rftoolsstorage.modules.craftingmanager.system.CraftingQueue;
import mcjty.rftoolsstorage.modules.craftingmanager.system.CraftingRequest;
import mcjty.rftoolsstorage.modules.craftingmanager.system.CraftingSystem;
import mcjty.rftoolsstorage.modules.craftingmanager.system.ICraftingDevice;
import mcjty.rftoolsstorage.modules.scanner.client.StorageScannerInformationRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;

import static mcjty.rftoolsbase.modules.informationscreen.blocks.DefaultPowerInformationScreenInfo.ENERGY;
import static mcjty.rftoolsbase.modules.informationscreen.blocks.DefaultPowerInformationScreenInfo.MAXENERGY;

public class StorageScannerInformationScreenInfo implements IInformationScreenInfo {

    public static final int MODE_CRAFTING = 2;

    public static final Key<ItemStack> CRAFT1 = new Key<>("craft1", Type.ITEMSTACK);
    public static final Key<ItemStack> CRAFT2 = new Key<>("craft2", Type.ITEMSTACK);
    public static final Key<ItemStack> CRAFT3 = new Key<>("craft3", Type.ITEMSTACK);
    public static final Key<ItemStack> CRAFT4 = new Key<>("craft4", Type.ITEMSTACK);
    public static final Key<ItemStack> CRAFT5 = new Key<>("craft5", Type.ITEMSTACK);
    public static final Key<ItemStack> CRAFT6 = new Key<>("craft6", Type.ITEMSTACK);
    public static final Key<Boolean> CRAFT1_ERROR = new Key<>("craft1error", Type.BOOLEAN);
    public static final Key<Boolean> CRAFT2_ERROR = new Key<>("craft2error", Type.BOOLEAN);
    public static final Key<Boolean> CRAFT3_ERROR = new Key<>("craft3error", Type.BOOLEAN);
    public static final Key<Boolean> CRAFT4_ERROR = new Key<>("craft4error", Type.BOOLEAN);
    public static final Key<Boolean> CRAFT5_ERROR = new Key<>("craft5error", Type.BOOLEAN);
    public static final Key<Boolean> CRAFT6_ERROR = new Key<>("craft6error", Type.BOOLEAN);

    public static final Pair<Key<ItemStack>,Key<Boolean>> CRAFT_KEYS[] = new Pair[]{
            Pair.of(CRAFT1, CRAFT1_ERROR),
            Pair.of(CRAFT2, CRAFT2_ERROR),
            Pair.of(CRAFT3, CRAFT3_ERROR),
            Pair.of(CRAFT4, CRAFT4_ERROR),
            Pair.of(CRAFT5, CRAFT5_ERROR),
            Pair.of(CRAFT6, CRAFT6_ERROR)
    };

    private final StorageScannerTileEntity scanner;

    public StorageScannerInformationScreenInfo(StorageScannerTileEntity scanner) {
        this.scanner = scanner;
    }

    @Override
    public int[] getSupportedModes() {
        return new int[]{MODE_POWER, MODE_POWER_GRAPHICAL, MODE_CRAFTING};
    }

    @Override
    public void tick() {
    }

    @Nonnull
    @Override
    public TypedMap getInfo(int mode) {
        if (mode == MODE_POWER || mode == MODE_POWER_GRAPHICAL) {
            return scanner.getCapability(CapabilityEnergy.ENERGY).map(h -> {
                return TypedMap.builder()
                        .put(ENERGY, (long) h.getEnergyStored())
                        .put(MAXENERGY, (long) h.getMaxEnergyStored())
                        .build();
            }).orElse(TypedMap.EMPTY);
        } else {
            TypedMap.Builder builder = TypedMap.builder();
            int idx = 0;

            CraftingSystem craftingSystem = scanner.getCraftingSystem();
            World world = scanner.getWorld();
            for (BlockPos pos : scanner.getCraftingInventories()) {
                if (idx >= CRAFT_KEYS.length) {
                    break;
                }
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof CraftingManagerTileEntity) {
                    CraftingManagerTileEntity craftingManager = (CraftingManagerTileEntity) te;
                    for (CraftingQueue queue : craftingManager.getQueues()) {
                        ICraftingDevice device = queue.getDevice();
                        if (device != null) {
                            if (device.getStatus() == ICraftingDevice.Status.BUSY) {
                                add(builder, idx++, device.getCraftingItem(), false);
                            }
                        }
                    }
                }
            }

            List<CraftingRequest> failedRequests = craftingSystem.getFailedRequests();
            for (CraftingRequest request : failedRequests) {
                if (idx >= CRAFT_KEYS.length) {
                    break;
                }
                ItemStack[] stacks = request.getIngredient().getMatchingStacks();
                if (stacks.length > 0) {
                    add(builder, idx++, stacks[0], true);
                }
            }
            return builder.build();
        }
    }

    private static void add(TypedMap.Builder builder, int idx, ItemStack stack, boolean error) {
        builder.put(CRAFT_KEYS[idx].getLeft(), stack);
        builder.put(CRAFT_KEYS[idx].getRight(), error);
    }

    @Override
    public void render(int mode, MatrixStack matrixStack, IRenderTypeBuffer buffer, @Nonnull TypedMap data, Direction orientation, double x, double y, double z, double scale) {
        if (mode == MODE_POWER) {
            DefaultPowerInformationRenderer.renderDefault(matrixStack, buffer, data, orientation, x, y, z, scale);
        } else if (mode == MODE_POWER_GRAPHICAL) {
            DefaultPowerInformationRenderer.renderGraphical(matrixStack, buffer, data, orientation, x, y, z, scale);
        } else {
            StorageScannerInformationRenderer.renderCrafting(matrixStack, buffer, data, orientation, x, y, z, scale);
        }
    }
}
