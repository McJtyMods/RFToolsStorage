package mcjty.rftoolsstorage.modules.scanner.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import mcjty.rftoolsstorage.modules.scanner.StorageScannerModule;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record StorageControlScreenModule(GlobalPos pos, boolean starred, int dirty, List<ItemStack> stacks) implements IScreenModule<StorageControlScreenModule, StorageControlScreenModule.ModuleDataStacks>, ITooltipInfo,
        IScreenModuleUpdater {

    // @todo 1.15 to replace the oredict from the past we might need a way to set a tag here

    public static final StorageControlScreenModule DEFAULT = new StorageControlScreenModule(GlobalPos.of(Level.OVERWORLD, BlockPosTools.INVALID), false, -1, Collections.nCopies(9, ItemStack.EMPTY));

    public static final Codec<StorageControlScreenModule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.fieldOf("pos").forGetter(StorageControlScreenModule::pos),
            Codec.BOOL.fieldOf("starred").forGetter(StorageControlScreenModule::starred),
            Codec.INT.fieldOf("dirty").forGetter(StorageControlScreenModule::dirty),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("stacks").forGetter(StorageControlScreenModule::stacks)
    ).apply(instance, StorageControlScreenModule::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StorageControlScreenModule> STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC, StorageControlScreenModule::pos,
            ByteBufCodecs.BOOL, StorageControlScreenModule::starred,
            ByteBufCodecs.INT, StorageControlScreenModule::dirty,
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), StorageControlScreenModule::stacks,
            StorageControlScreenModule::new);

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
        public void writeToBuf(RegistryFriendlyByteBuf buf) {
            buf.writeInt(amounts.length);
            for (int i : amounts) {
                buf.writeInt(i);
            }

        }
    }

    @Override
    public ModuleDataStacks getData(IScreenDataHelper helper, Level worldObj, long millis) {
        IStorageScanner scannerTileEntity = getStorageScanner(worldObj, pos.dimension(), pos.pos());
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
    public StorageControlScreenModule validate(Level world, BlockPos pos, boolean isPlus) {
        return this;
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
        IStorageScanner te = getStorageScanner(world, pos.dimension(), pos.pos());
        if (te != null) {
            int i = getHighlightedStack(x, y);
            if (i != -1 && !stacks.get(i).isEmpty()) {
                return Collections.singletonList(ChatFormatting.GREEN + "Item: " + ChatFormatting.WHITE + stacks.get(i).getHoverName());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public int getRfPerTick() {
        return StorageScannerConfiguration.STORAGE_CONTROL_RFPERTICK.get();
    }

    public StorageControlScreenModule withPos(GlobalPos pos) {
        return new StorageControlScreenModule(pos, starred, dirty, stacks);
    }

    public StorageControlScreenModule withStack(int index, ItemStack stack) {
        List<ItemStack> newStacks = new ArrayList<>(stacks);
        newStacks.set(index, stack);
        return new StorageControlScreenModule(pos, starred, dirty, newStacks);
    }

    public StorageControlScreenModule withStarred(boolean starred) {
        return new StorageControlScreenModule(pos, starred, dirty, stacks);
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
        return ItemStack.isSameItem(thisItem, other);
    }


    @Override
    public ItemStack update(ItemStack module, Level world, Player player) {
        if (dirty >= 0) {
            ItemStack copy = module.copy();
            StorageControlScreenModule data = copy.get(StorageScannerModule.MODULE_CONTROL_DATA).withStack(dirty, stacks.get(dirty)).withDirty(-1);
            copy.set(StorageScannerModule.MODULE_CONTROL_DATA, data);
            if (player != null) {
                SoundTools.playSound(player.getCommandSenderWorld(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                        player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ(), 1.0f, 1.0f);
            }
            return copy;
        }
        return ItemStack.EMPTY;
    }

    private StorageControlScreenModule withDirty(int dirty) {
        return new StorageControlScreenModule(pos, starred, dirty, stacks);
    }

    @Override
    public ItemStack mouseClick(ItemStack moduleStack, Level world, int hitx, int hity, boolean clicked, Player player) {
        if ((!clicked) || player == null) {
            return ItemStack.EMPTY;
        }
        if (BlockPosTools.INVALID.equals(pos.pos())) {
            player.displayClientMessage(ComponentFactory.literal(ChatFormatting.RED + "Module is not linked to storage scanner!"), false);
            return ItemStack.EMPTY;
        }
        IStorageScanner scannerTileEntity = getStorageScanner(player.level(), pos.dimension(), pos.pos());
        if (scannerTileEntity == null) {
            return ItemStack.EMPTY;
        }
        if (hitx >= 0) {
            boolean insertStackActive = hitx >= 0 && hitx < 60 && hity > 98;
            if (insertStackActive) {
                if (isShown(player.getItemInHand(InteractionHand.MAIN_HAND))) {
                    ItemStack stack = scannerTileEntity.injectStackFromScreen(player.getItemInHand(InteractionHand.MAIN_HAND), player);
                    player.setItemInHand(InteractionHand.MAIN_HAND, stack);
                }
                player.containerMenu.broadcastChanges();
                return ItemStack.EMPTY;
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
                return ItemStack.EMPTY;
            }

            int i = getHighlightedStack(hitx, hity);
            if (i != -1) {
                if (stacks.get(i).isEmpty()) {
                    ItemStack heldItem = player.getMainHandItem();
                    if (!heldItem.isEmpty()) {
                        StorageControlScreenModule data = withStack(i, heldItem.copy()).withDirty(i);
                        moduleStack.set(StorageScannerModule.MODULE_CONTROL_DATA, data);
                        return moduleStack;
                    }
                } else {
                    scannerTileEntity.giveToPlayerFromScreen(stacks.get(i), player.isShiftKeyDown(), player);
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
