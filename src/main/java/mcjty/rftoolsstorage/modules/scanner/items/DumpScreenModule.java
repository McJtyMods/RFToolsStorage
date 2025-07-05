package mcjty.rftoolsstorage.modules.scanner.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record DumpScreenModule(GlobalPos pos, boolean active, List<ItemStack> stacks, boolean matchingTag, String line, int color) implements IScreenModule<DumpScreenModule, IModuleData> {

    public static final int COLS = 7;
    public static final int ROWS = 4;

    public static final DumpScreenModule DEFAULT = new DumpScreenModule(GlobalPos.of(Level.OVERWORLD, BlockPosTools.INVALID),  false, Collections.nCopies(COLS * ROWS, ItemStack.EMPTY), false, "", 0xffffff);

    public static final Codec<DumpScreenModule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.fieldOf("pos").forGetter(DumpScreenModule::pos),
            Codec.BOOL.fieldOf("active").forGetter(DumpScreenModule::active),
            ItemStack.CODEC.listOf().fieldOf("stacks").forGetter(DumpScreenModule::stacks),
            Codec.BOOL.fieldOf("matchingTag").forGetter(DumpScreenModule::matchingTag),
            Codec.STRING.fieldOf("line").forGetter(DumpScreenModule::line),
            Codec.INT.fieldOf("color").forGetter(DumpScreenModule::color)
    ).apply(instance, DumpScreenModule::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DumpScreenModule> STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC, DumpScreenModule::pos,
            ByteBufCodecs.BOOL, DumpScreenModule::active,
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), DumpScreenModule::stacks,
            ByteBufCodecs.BOOL, DumpScreenModule::matchingTag,
            ByteBufCodecs.STRING_UTF8, DumpScreenModule::line,
            ByteBufCodecs.INT, DumpScreenModule::color,
            DumpScreenModule::new);

    @Override
    public IModuleDataBoolean getData(IScreenDataHelper helper, Level worldObj, long millis) {
        return null;
    }

    public DumpScreenModule withActive(boolean active) {
        return new DumpScreenModule(pos, active, stacks, matchingTag, line, color);
    }

    public DumpScreenModule withLine(String line) {
        return new DumpScreenModule(pos, active, stacks, matchingTag, line, color);
    }

    public DumpScreenModule withColor(int color) {
        return new DumpScreenModule(pos, active, stacks, matchingTag, line, color);
    }

    public DumpScreenModule withMatchingTag(boolean matchingTag) {
        return new DumpScreenModule(pos, active, stacks, matchingTag, line, color);
    }

    public DumpScreenModule withStack(int index, ItemStack stack) {
        List<ItemStack> newstacks = new ArrayList<>(stacks);
        newstacks.set(index, stack);
        return new DumpScreenModule(pos, active, newstacks, matchingTag, line, color);
    }

    @Override
    public DumpScreenModule validate(Level world, BlockPos p, boolean isPlus) {
        if (isPlus) {
            return withActive(true);
        }
        // To check if this is active we need to check that the coordinate in this module is correct,
        // the dimension is equal and the coordinate is not too far from the given position (max 64 blocks)
        if (LevelTools.isLoaded(world, pos.pos())) {
            if (Objects.equals(pos.dimension(), world.dimension())) {
                int dx = Math.abs(pos.pos().getX() - p.getX());
                int dy = Math.abs(pos.pos().getY() - p.getY());
                int dz = Math.abs(pos.pos().getZ() - p.getZ());
                if (dx <= 64 && dy <= 64 && dz <= 64) {
                    return withActive(true);
                }
            }
        }
        return withActive(false);
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
    public ItemStack mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked, Player player) {
        if ((!clicked) || player == null) {
            return ItemStack.EMPTY;
        }
        if (BlockPosTools.INVALID.equals(pos.pos())) {
            player.displayClientMessage(ComponentFactory.literal(ChatFormatting.RED + "Module is not linked to storage scanner!"), false);
            return ItemStack.EMPTY;
        }

        IStorageScanner scannerTileEntity = StorageControlScreenModule.getStorageScanner(world, pos.dimension(), pos.pos());
        if (scannerTileEntity == null) {
            return ItemStack.EMPTY;
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
        return ItemStack.EMPTY;
    }

    @Override
    public int getRfPerTick() {
        return StorageScannerConfiguration.DUMP_RFPERTICK.get();
    }
}
