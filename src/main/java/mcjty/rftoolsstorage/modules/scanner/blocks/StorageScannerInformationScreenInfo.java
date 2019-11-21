package mcjty.rftoolsstorage.modules.scanner.blocks;

import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsbase.api.infoscreen.IInformationScreenInfo;
import mcjty.rftoolsbase.modules.informationscreen.client.DefaultPowerInformationRenderer;
import mcjty.rftoolsstorage.modules.craftingmanager.system.CraftingRequest;
import mcjty.rftoolsstorage.modules.craftingmanager.system.CraftingSystem;
import mcjty.rftoolsstorage.modules.scanner.client.StorageScannerInformationRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
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
            CraftingSystem craftingSystem = scanner.getCraftingSystem();
            List<CraftingRequest> failedRequests = craftingSystem.getFailedRequests();
            TypedMap.Builder builder = TypedMap.builder();
            for (int i = 0 ; i < 6 ; i++) {
                builder.put(CRAFT_KEYS[i].getLeft(), ItemStack.EMPTY);
                if (i < failedRequests.size()) {
                    ItemStack[] stacks = failedRequests.get(i).getIngredient().getMatchingStacks();
                    if (stacks.length > 0) {
                        builder.put(CRAFT_KEYS[i].getLeft(), stacks[0]);
                        builder.put(CRAFT_KEYS[i].getRight(), true);
                    }
                }
            }
            return builder.build();
        }
    }

    @Override
    public void render(int mode, @Nonnull TypedMap data, Direction orientation, double x, double y, double z, double scale) {
        if (mode == MODE_POWER) {
            DefaultPowerInformationRenderer.renderDefault(data, orientation, x, y, z, scale);
        } else if (mode == MODE_POWER_GRAPHICAL) {
            DefaultPowerInformationRenderer.renderGraphical(data, orientation, x, y, z, scale);
        } else {
            StorageScannerInformationRenderer.renderCrafting(data, orientation, x, y, z, scale);
        }
    }
}
