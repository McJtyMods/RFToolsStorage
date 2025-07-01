package mcjty.rftoolsstorage.compat.xnet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.gui.IEditorGui;
import mcjty.rftoolsbase.api.xnet.gui.IndicatorIcon;
import mcjty.rftoolsbase.api.xnet.helper.AbstractConnectorSettings;
import mcjty.rftoolsstorage.modules.scanner.tools.InventoryAccessSettings;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class StorageConnectorSettings extends AbstractConnectorSettings {

    public static final String TAG_MODE = "mode";
    public static final String TAG_BIGUI = "bigui";
    public static final String TAG_BIAUTO = "biauto";
    public static final String TAG_BISCREEN = "biscreen";
    public static final String TAG_BOGUI = "bogui";
    public static final String TAG_BOAUTO = "boauto";
    public static final String TAG_BOSCREEN = "boscreen";
    public static final String TAG_FILTER = "flt";
    public static final String TAG_BLACKLIST = "blacklist";
//    public static final String TAG_OREDICT = "od";
    public static final String TAG_NBT = "nbt";
    public static final String TAG_META = "meta";

    public enum Mode implements StringRepresentable{
        INVENTORY,
        STORAGE;

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);
        public static final StreamCodec<FriendlyByteBuf, Mode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(Mode.class);

        @Override
        public String getSerializedName() {
            return name();
        }
    }

    private Mode mode = Mode.INVENTORY;
    private InventoryAccessSettings accessSettings = new InventoryAccessSettings();

    public StorageConnectorSettings(@Nonnull Direction side) {
        super(AbstractConnectorSettings.DEFAULT_SETTINGS, side);
    }

    public Mode getMode() {
        return mode;
    }

    public InventoryAccessSettings getAccessSettings() {
        return accessSettings;
    }

    public static final MapCodec<StorageConnectorSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BaseSettings.CODEC.fieldOf("base").forGetter(settings -> settings.settings),
            InventoryAccessSettings.CODEC.fieldOf("as").forGetter(settings -> settings.accessSettings),
            Mode.CODEC.fieldOf("mode").forGetter(settings -> settings.mode)
            ).apply(instance, StorageConnectorSettings::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StorageConnectorSettings> STREAM_CODEC = StreamCodec.composite(
            BaseSettings.STREAM_CODEC, settings -> settings.settings,
            InventoryAccessSettings.STREAM_CODEC, settings -> settings.accessSettings,
            Mode.STREAM_CODEC, settings -> settings.mode,
            StorageConnectorSettings::new);

    public StorageConnectorSettings(BaseSettings baseSettings, InventoryAccessSettings accessSettings, Mode mode) {
        super(baseSettings, Direction.NORTH);
        this.accessSettings = accessSettings;
        this.mode = mode;
    }

    @Override
    public IChannelType getType() {
        return XNetSupport.storageChannelType;
    }

    @Nullable
    @Override
    public IndicatorIcon getIndicatorIcon() {
        switch (mode) {
            case INVENTORY:
                boolean inputBlocked = accessSettings.inputBlocked();
                boolean outputBlocked = accessSettings.outputBlocked();
                if (inputBlocked && outputBlocked) {
                    return new IndicatorIcon(StorageChannelSettings.iconGuiElements, 13, 75, 13, 10);
                } else if (inputBlocked) {
                    return new IndicatorIcon(StorageChannelSettings.iconGuiElements, 13, 48, 13, 10);
                } else if (outputBlocked) {
                    return new IndicatorIcon(StorageChannelSettings.iconGuiElements, 0, 48, 13, 10);
                } else {
                    return new IndicatorIcon(StorageChannelSettings.iconGuiElements, 13, 57, 13, 10);
                }
            case STORAGE:
                return new IndicatorIcon(StorageChannelSettings.iconGuiElements, 13, 66, 13, 10);
        }
        return null;
    }

    @Nullable
    @Override
    public String getIndicator() {
        return null;
    }

    @Override
    public boolean isEnabled(String tag) {
        if (tag.equals(TAG_FACING)) {
            return advanced;
        }
        return true;
    }

    @Override
    public void createGui(IEditorGui gui) {
        advanced = gui.isAdvanced();
        sideGui(gui);
        gui.shift(30)
                .choices(TAG_MODE, "Set to 'inventory' or 'storage'", mode, Mode.values());
        if (mode == Mode.INVENTORY) {
            gui.nl()
                    .label("Input ")
                    .shift(12)
                    .label("UI")
                    .toggle(TAG_BIGUI, "Block input from scanner UI", accessSettings.isBlockInputGui())
                    .label("Auto")
                    .toggle(TAG_BIAUTO, "Block input from automation", accessSettings.isBlockInputAuto())
                    .label("Scr")
                    .toggle(TAG_BISCREEN, "Block input from screens", accessSettings.isBlockInputScreen());
            gui.nl()
                    .label("Output")
                    .shift(10)
                    .label("UI")
                    .toggle(TAG_BOGUI, "Block extraction from scanner UI", accessSettings.isBlockOutputGui())
                    .label("Auto")
                    .toggle(TAG_BOAUTO, "Block extraction from automation", accessSettings.isBlockOutputAuto())
                    .label("Scr")
                    .toggle(TAG_BOSCREEN, "Block extraction from screens", accessSettings.isBlockOutputScreen());
            gui.nl()
                    .toggleText(TAG_BLACKLIST, "Enable blacklist mode", "BL", accessSettings.isBlacklist()).shift(2)
//                    .toggleText(TAG_OREDICT, "Ore dictionary matching", "Ore", accessSettings.isOredictMode()).shift(2)
                    .toggleText(TAG_META, "Metadata matching", "Meta", accessSettings.isMetaMode()).shift(2)
                    .toggleText(TAG_NBT, "NBT matching", "NBT", accessSettings.isNbtMode())
                    .nl();
            for (int i = 0 ; i < InventoryAccessSettings.FILTER_SIZE ; i++) {
                gui.ghostSlot(TAG_FILTER + i, accessSettings.getFilters().get(i));
            }
        }
    }

    private static boolean toBool(Object o) {
        if (o instanceof Boolean) {
            return (Boolean) o;
        } else {
            return false;
        }
    }

    @Override
    public void update(Map<String, Object> data) {
        super.update(data);
        mode = Mode.valueOf(((String)data.get(TAG_MODE)).toUpperCase());
        accessSettings.setBlockInputGui(toBool(data.get(TAG_BIGUI)));
        accessSettings.setBlockInputAuto(toBool(data.get(TAG_BIAUTO)));
        accessSettings.setBlockInputScreen(toBool(data.get(TAG_BISCREEN)));
        accessSettings.setBlockOutputGui(toBool(data.get(TAG_BOGUI)));
        accessSettings.setBlockOutputAuto(toBool(data.get(TAG_BOAUTO)));
        accessSettings.setBlockOutputScreen(toBool(data.get(TAG_BOSCREEN)));

        for (int i = 0 ; i < InventoryAccessSettings.FILTER_SIZE ; i++) {
            accessSettings.getFilters().set(i, (ItemStack) data.get(TAG_FILTER+i));
        }

//        accessSettings.setOredictMode(toBool(data.get(TAG_OREDICT)));
        accessSettings.setMetaMode(toBool(data.get(TAG_META)));
        accessSettings.setNbtMode(toBool(data.get(TAG_NBT)));
        accessSettings.setBlacklist(toBool(data.get(TAG_BLACKLIST)));

    }
}
