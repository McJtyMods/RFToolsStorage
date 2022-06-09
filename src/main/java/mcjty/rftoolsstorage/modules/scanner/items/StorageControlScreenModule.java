package mcjty.rftoolsstorage.modules.scanner.items;

import io.netty.buffer.ByteBuf;
import mcjty.lib.varia.*;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.IScreenModuleUpdater;
import mcjty.rftoolsbase.api.screens.ITooltipInfo;
import mcjty.rftoolsbase.api.screens.data.IModuleData;
import mcjty.rftoolsbase.api.storage.IStorageScanner;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerConfiguration;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Collections;
import java.util.List;

public class StorageControlScreenModule implements IScreenModule<StorageControlScreenModule.ModuleDataStacks>, ITooltipInfo,
        IScreenModuleUpdater {
    private final ItemStackList stacks = ItemStackList.create(9);

    protected ResourceKey<Level> dim = Level.OVERWORLD;
    protected BlockPos coordinate = BlockPosTools.INVALID;
    private boolean starred = false;
    private int dirty = -1;

    // @todo 1.15 to replace the oredict from the past we might need a way to set a tag here

    public static class ModuleDataStacks implements IModuleData {

        public static final String ID = RFToolsStorage.MODID + ":storage";

        private int[] amounts = null;

        @Override
        public String getId() {
            return ID;
        }

        public ModuleDataStacks(int... amountsIn) {
            amounts = amountsIn;
        }

        public ModuleDataStacks(ByteBuf buf) {
            int s = buf.readInt();
            amounts = new int[s];
            for (int i = 0; i < s; i++) {
                amounts[i] = buf.readInt();
            }
        }

        public int getAmount(int idx) {
            return amounts[idx];
        }

        @Override
        public void writeToBuf(FriendlyByteBuf buf) {
            buf.writeInt(amounts.length);
            for (int i : amounts) {
                buf.writeInt(i);
            }

        }
    }

    @Override
    public ModuleDataStacks getData(IScreenDataHelper helper, Level worldObj, long millis) {
        IStorageScanner scannerTileEntity = getStorageScanner(worldObj, dim, coordinate);
        if (scannerTileEntity == null) {
            return null;
        }
        int[] amounts = new int[stacks.size()];
        for (int i = 0; i < stacks.size(); i++) {
            amounts[i] = scannerTileEntity.countItems(stacks.get(i), starred);
        }
        return new ModuleDataStacks(amounts);
    }

    public static IStorageScanner getStorageScanner(Level worldObj, ResourceKey<Level> dim, BlockPos coordinate) {
        Level world = LevelTools.getLevel(worldObj, dim);
        if (world == null) {
            return null;
        }

        if (!LevelTools.isLoaded(world, coordinate)) {
            return null;
        }

        BlockEntity te = world.getBlockEntity(coordinate);
        if (te == null) {
            return null;
        }

        if (!(te instanceof IStorageScanner)) {
            return null;
        }

        return (IStorageScanner) te;
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
        IStorageScanner te = getStorageScanner(LevelTools.getOverworld(), dim, coordinate);
        if (te != null) {
            te.clearCachedCounts();
        }
    }

    private int getHighlightedStack(int hitx, int hity) {
        int i = 0;
        for (int yy = 0; yy < 3; yy++) {
            int y = 7 + yy * 35;
            for (int xx = 0; xx < 3; xx++) {
                int x = xx * 40;

                boolean hilighted = hitx >= x + 8 && hitx <= x + 38 && hity >= y - 7 && hity <= y + 22;
                if (hilighted) {
                    return i;
                }
                i++;
            }
        }
        return -1;
    }

    @Override
    public List<String> getInfo(Level world, int x, int y) {
        IStorageScanner te = getStorageScanner(world, dim, coordinate);
        if (te != null) {
            int i = getHighlightedStack(x, y);
            if (i != -1 && !stacks.get(i).isEmpty()) {
                return Collections.singletonList(ChatFormatting.GREEN + "Item: " + ChatFormatting.WHITE + stacks.get(i).getHoverName());
            }
        }
        return Collections.emptyList();
    }

    protected void setupCoordinateFromNBT(CompoundTag tagCompound, ResourceKey<Level> dim, BlockPos pos) {
        coordinate = BlockPosTools.INVALID;
        starred = tagCompound.getBoolean("starred");
        if (tagCompound.contains("monitorx")) {
            this.dim = LevelTools.getId(tagCompound.getString("monitordim"));
            BlockPos c = new BlockPos(tagCompound.getInt("monitorx"), tagCompound.getInt("monitory"), tagCompound.getInt("monitorz"));
            int dx = Math.abs(c.getX() - pos.getX());
            int dy = Math.abs(c.getY() - pos.getY());
            int dz = Math.abs(c.getZ() - pos.getZ());
            coordinate = c;
        }
    }

    @Override
    public int getRfPerTick() {
        return StorageScannerConfiguration.STORAGE_CONTROL_RFPERTICK.get();
    }


    private boolean isShown(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (ItemStack s : stacks) {
            if (isItemEqual(stack, s)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isItemEqual(ItemStack thisItem, ItemStack other) {
        if (other.isEmpty()) {
            return false;
        }
        return thisItem.sameItem(other);
    }


    @Override
    public CompoundTag update(CompoundTag tagCompound, Level world, Player player) {
        if (dirty >= 0) {
            CompoundTag newCompound = tagCompound.copy();
            CompoundTag tc = new CompoundTag();
            stacks.get(dirty).save(tc);
            newCompound.put("stack" + dirty, tc);
            if (player != null) {
                SoundTools.playSound(player.getCommandSenderWorld(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                        player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ(), 1.0f, 1.0f);
            }
            dirty = -1;
            return newCompound;
        }
        return null;
    }

    @Override
    public void mouseClick(Level world, int hitx, int hity, boolean clicked, Player player) {
        if ((!clicked) || player == null) {
            return;
        }
        if (BlockPosTools.INVALID.equals(coordinate)) {
            player.displayClientMessage(ComponentFactory.literal(ChatFormatting.RED + "Module is not linked to storage scanner!"), false);
            return;
        }
        IStorageScanner scannerTileEntity = getStorageScanner(player.level, dim, coordinate);
        if (scannerTileEntity == null) {
            return;
        }
        if (hitx >= 0) {
            boolean insertStackActive = hitx >= 0 && hitx < 60 && hity > 98;
            if (insertStackActive) {
                if (isShown(player.getItemInHand(InteractionHand.MAIN_HAND))) {
                    ItemStack stack = scannerTileEntity.injectStackFromScreen(player.getItemInHand(InteractionHand.MAIN_HAND), player);
                    player.setItemInHand(InteractionHand.MAIN_HAND, stack);
                }
                player.containerMenu.broadcastChanges();
                return;
            }

            boolean insertAllActive = hitx >= 60 && hity > 98;
            if (insertAllActive) {
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    if (isShown(player.getInventory().getItem(i))) {
                        ItemStack stack = scannerTileEntity.injectStackFromScreen(player.getInventory().getItem(i), player);
                        player.getInventory().setItem(i, stack);
                    }
                }
                player.containerMenu.broadcastChanges();
                return;
            }

            int i = getHighlightedStack(hitx, hity);
            if (i != -1) {
                if (stacks.get(i).isEmpty()) {
                    ItemStack heldItem = player.getMainHandItem();
                    if (!heldItem.isEmpty()) {
                        ItemStack stack = heldItem.copy();
                        stack.setCount(1);
                        stacks.set(i, stack);
                        dirty = i;
                    }
                } else {
                    scannerTileEntity.giveToPlayerFromScreen(stacks.get(i), player.isShiftKeyDown(), player);
                }
            }
        }
    }
}
