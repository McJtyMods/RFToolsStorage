package mcjty.rftoolsstorage.modules.scanner.items;

import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.ItemStackList;
import mcjty.lib.varia.ItemStackTools;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.data.IModuleData;
import mcjty.rftoolsbase.api.screens.data.IModuleDataBoolean;
import mcjty.rftoolsbase.api.storage.IStorageScanner;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerConfiguration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.stream.Collectors;

public class DumpScreenModule implements IScreenModule<IModuleData> {

    public static final int COLS = 7;
    public static final int ROWS = 4;

    private final ItemStackList stacks = ItemStackList.create(COLS * ROWS);
    protected ResourceKey<Level> dim = Level.OVERWORLD;
    protected BlockPos coordinate = BlockPosTools.INVALID;
    private boolean matchingTag = false;

    @Override
    public IModuleDataBoolean getData(IScreenDataHelper helper, Level worldObj, long millis) {
        return null;
    }

    @Override
    public void setupFromNBT(CompoundTag tagCompound, ResourceKey<Level> dim, BlockPos pos) {
        if (tagCompound != null) {
            setupCoordinateFromNBT(tagCompound, dim, pos);
            for (int i = 0; i < stacks.size(); i++) {
                if (tagCompound.contains("stack" + i)) {
                    stacks.set(i, ItemStack.of(tagCompound.getCompound("stack" + i)));
                }
            }
        }
    }

    protected void setupCoordinateFromNBT(CompoundTag tagCompound, ResourceKey<Level> dim, BlockPos pos) {
        coordinate = BlockPosTools.INVALID;
        matchingTag = tagCompound.getBoolean("matchingTag");
        if (tagCompound.contains("monitorx")) {
            this.dim = LevelTools.getId(tagCompound.getString("monitordim"));
            if (Objects.equals(dim, this.dim)) {
                BlockPos c = new BlockPos(tagCompound.getInt("monitorx"), tagCompound.getInt("monitory"), tagCompound.getInt("monitorz"));
                int dx = Math.abs(c.getX() - pos.getX());
                int dy = Math.abs(c.getY() - pos.getY());
                int dz = Math.abs(c.getZ() - pos.getZ());
                if (dx <= 64 && dy <= 64 && dz <= 64) {
                    coordinate = c;
                }
            }
        }
    }

    private boolean isShown(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (ItemStack s : stacks) {
            if (isItemEqual(stack, s)) {
                return true;
            }
            if (matchingTag) {
                if (ItemStackTools.hasCommonTag(s.getItem().builtInRegistryHolder().tags().collect(Collectors.toSet()))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isItemEqual(ItemStack thisItem, ItemStack other) {
        if (other.isEmpty()) {
            return false;
        }
        return thisItem.sameItem(other);
    }

    @Override
    public void mouseClick(Level world, int x, int y, boolean clicked, Player player) {
        if ((!clicked) || player == null) {
            return;
        }
        if (BlockPosTools.INVALID.equals(coordinate)) {
            player.displayClientMessage(new TextComponent(ChatFormatting.RED + "Module is not linked to storage scanner!"), false);
            return;
        }

        IStorageScanner scannerTileEntity = StorageControlScreenModule.getStorageScanner(world, dim, coordinate);
        if (scannerTileEntity == null) {
            return;
        }
        int xoffset = 5;
        if (x >= xoffset) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (isShown(player.getInventory().getItem(i))) {
                    ItemStack stack = scannerTileEntity.injectStackFromScreen(player.getInventory().getItem(i), player);
                    player.getInventory().setItem(i, stack);
                }
            }
            player.containerMenu.broadcastChanges();
        }
    }

    @Override
    public int getRfPerTick() {
        return StorageScannerConfiguration.DUMP_RFPERTICK.get();
    }
}
