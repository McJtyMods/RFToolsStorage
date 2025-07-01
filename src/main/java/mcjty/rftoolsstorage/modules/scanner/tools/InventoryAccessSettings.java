package mcjty.rftoolsstorage.modules.scanner.tools;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.lib.varia.ItemStackList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

public class InventoryAccessSettings {
    private boolean blockInputGui = false;
    private boolean blockInputAuto = false;
    private boolean blockInputScreen = false;
    private boolean blockOutputGui = false;
    private boolean blockOutputAuto = false;
    private boolean blockOutputScreen = false;

    public static final int FILTER_SIZE = 18;
    private boolean metaMode = false;
    private boolean nbtMode = false;
    private boolean blacklist = false;
    private final ItemStackList filters = ItemStackList.create(FILTER_SIZE);

    public static final MapCodec<InventoryAccessSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("biInGui").forGetter(settings -> settings.blockInputGui),
            Codec.BOOL.fieldOf("biInAuto").forGetter(settings -> settings.blockInputAuto),
            Codec.BOOL.fieldOf("biInScreen").forGetter(settings -> settings.blockInputScreen),
            Codec.BOOL.fieldOf("biOutGui").forGetter(settings -> settings.blockOutputGui),
            Codec.BOOL.fieldOf("biOutAuto").forGetter(settings -> settings.blockOutputGui),
            Codec.BOOL.fieldOf("biOutScreen").forGetter(settings -> settings.blockOutputGui),
            Codec.BOOL.fieldOf("meta").forGetter(settings -> settings.metaMode),
            Codec.BOOL.fieldOf("comp").forGetter(settings -> settings.nbtMode),
            Codec.BOOL.fieldOf("blacklist").forGetter(settings -> settings.blacklist),
            ItemStack.CODEC.listOf().fieldOf("filters").forGetter(settings -> settings.filters)
    ).apply(instance, InventoryAccessSettings::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InventoryAccessSettings> STREAM_CODEC = CompositeStreamCodec.composite(
            ByteBufCodecs.BOOL, settings -> settings.blockInputGui,
            ByteBufCodecs.BOOL, settings -> settings.blockInputAuto,
            ByteBufCodecs.BOOL, settings -> settings.blockInputScreen,
            ByteBufCodecs.BOOL, settings -> settings.blockOutputGui,
            ByteBufCodecs.BOOL, settings -> settings.blockOutputGui,
            ByteBufCodecs.BOOL, settings -> settings.blockOutputGui,
            ByteBufCodecs.BOOL, settings -> settings.metaMode,
            ByteBufCodecs.BOOL, settings -> settings.nbtMode,
            ByteBufCodecs.BOOL, settings -> settings.blacklist,
            ItemStack.LIST_STREAM_CODEC, settings -> settings.filters,
            InventoryAccessSettings::new
    );

    public InventoryAccessSettings(boolean blockInputGui, boolean blockInputAuto, boolean blockInputScreen,
                                   boolean blockOutputGui, boolean blockOutputAuto, boolean blockOutputScreen,
                                   boolean metaMode, boolean nbtMode, boolean blacklist, List<ItemStack> filters) {
        this.blockInputGui = blockInputGui;
        this.blockInputAuto = blockInputAuto;
        this.blockInputScreen = blockInputScreen;
        this.blockOutputGui = blockOutputGui;
        this.blockOutputAuto = blockOutputAuto;
        this.blockOutputScreen = blockOutputScreen;
        this.metaMode = metaMode;
        this.nbtMode = nbtMode;
        this.blacklist = blacklist;
        this.filters.clear();
        this.filters.addAll(filters);
    }

    public InventoryAccessSettings() {}

    // Cached matcher for items
    private Predicate<ItemStack> matcher = null;

    public ItemStackList getFilters() {
        return filters;
    }

    public boolean isMetaMode() {
        return metaMode;
    }

    public void setMetaMode(boolean metaMode) {
        this.metaMode = metaMode;
    }

    public boolean isNbtMode() {
        return nbtMode;
    }

    public void setNbtMode(boolean nbtMode) {
        this.nbtMode = nbtMode;
    }

    public boolean isBlacklist() {
        return blacklist;
    }

    public void setBlacklist(boolean blacklist) {
        this.blacklist = blacklist;
    }

    public boolean isBlockInputGui() {
        return blockInputGui;
    }

    public void setBlockInputGui(boolean blockInputGui) {
        this.blockInputGui = blockInputGui;
    }

    public boolean isBlockInputAuto() {
        return blockInputAuto;
    }

    public void setBlockInputAuto(boolean blockInputAuto) {
        this.blockInputAuto = blockInputAuto;
    }

    public boolean isBlockInputScreen() {
        return blockInputScreen;
    }

    public void setBlockInputScreen(boolean blockInputScreen) {
        this.blockInputScreen = blockInputScreen;
    }

    public boolean isBlockOutputGui() {
        return blockOutputGui;
    }

    public void setBlockOutputGui(boolean blockOutputGui) {
        this.blockOutputGui = blockOutputGui;
    }

    public boolean isBlockOutputScreen() {
        return blockOutputScreen;
    }

    public void setBlockOutputScreen(boolean blockOutputScreen) {
        this.blockOutputScreen = blockOutputScreen;
    }

    public boolean isBlockOutputAuto() {
        return blockOutputAuto;
    }

    public void setBlockOutputAuto(boolean blockOutputAuto) {
        this.blockOutputAuto = blockOutputAuto;
    }

    public boolean inputBlocked() {
        return blockInputGui || blockInputScreen || blockInputAuto;
    }

    public boolean outputBlocked() {
        return blockOutputGui || blockOutputScreen || blockOutputAuto;
    }

    public Predicate<ItemStack> getMatcher() {
        if (matcher == null) {
            ItemStackList filterList = ItemStackList.create();
            for (ItemStack stack : filters) {
                if (!stack.isEmpty()) {
                    filterList.add(stack);
                }
            }
            if (filterList.isEmpty()) {
                matcher = itemStack -> true;
            } else {
                ItemFilterCache filterCache = new ItemFilterCache(metaMode, blacklist, nbtMode, filterList);
                matcher = filterCache::match;
            }
        }
        return matcher;
    }

}
