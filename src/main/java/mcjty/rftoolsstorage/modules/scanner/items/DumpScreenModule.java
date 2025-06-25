package mcjty.rftoolsstorage.modules.scanner.items;

import com.mojang.serialization.Codec;
import mcjty.lib.varia.*;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.data.IModuleData;
import mcjty.rftoolsbase.api.screens.data.IModuleDataBoolean;
import mcjty.rftoolsbase.api.storage.IStorageScanner;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerConfiguration;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record DumpScreenModule(GlobalPos pos, List<ItemStack> stacks, boolean matchingTag) implements IScreenModule<DumpScreenModule, IModuleData> {

    public static final int COLS = 7;
    public static final int ROWS = 4;

    public static final DumpScreenModule DEFAULT = new DumpScreenModule(GlobalPos.of(Level.OVERWORLD, BlockPosTools.INVALID),  Collections.nCopies(COLS * ROWS, ItemStack.EMPTY), false);

    public static final Codec<DumpScreenModule> CODEC = Codec.record(DumpScreenModule::new,
            GlobalPos.CODEC.fieldOf("pos").forGetter(DumpScreenModule::pos),
            ItemStack.CODEC.listOf().fieldOf("stacks").forGetter(DumpScreenModule::stacks),
            Codec.BOOL.fieldOf("matchingTag").forGetter(DumpScreenModule::matchingTag));

    public static final StreamCodec<RegistryFriendlyByteBuf, DumpScreenModule> STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC, DumpScreenModule::pos,
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), DumpScreenModule::stacks,
            ByteBufCodecs.BOOL, DumpScreenModule::matchingTag,
            DumpScreenModule::new);

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
                // @todo more optimal?
                if (ItemStackTools.hasCommonTag(TagTools.getTags(s.getItem()))) {
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
        return ItemStack.isSameItem(thisItem, other);
    }

    @Override
    public void mouseClick(Level world, int x, int y, boolean clicked, Player player) {
        if ((!clicked) || player == null) {
            return;
        }
        if (BlockPosTools.INVALID.equals(coordinate)) {
            player.displayClientMessage(ComponentFactory.literal(ChatFormatting.RED + "Module is not linked to storage scanner!"), false);
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
