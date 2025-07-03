package mcjty.rftoolsstorage.modules.scanner.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.rftoolsstorage.modules.scanner.tools.SortingMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record StorageScannerData(int radius, boolean exportC, boolean wideview, SortingMode sortingMode) {

    public static final StorageScannerData DEFAULT = new StorageScannerData(1, false, true, SortingMode.NAME);

    public static final Codec<StorageScannerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("radius").forGetter(StorageScannerData::radius),
            Codec.BOOL.fieldOf("exportc").forGetter(StorageScannerData::exportC),
            Codec.BOOL.fieldOf("wideview").forGetter(StorageScannerData::wideview),
            SortingMode.CODEC.fieldOf("sortingMode").forGetter(StorageScannerData::sortingMode)
    ).apply(instance, StorageScannerData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StorageScannerData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, StorageScannerData::radius,
            ByteBufCodecs.BOOL, StorageScannerData::exportC,
            ByteBufCodecs.BOOL, StorageScannerData::wideview,
            SortingMode.STREAM_CODEC, StorageScannerData::sortingMode,
            StorageScannerData::new
    );

    public StorageScannerData withRadius(int radius) {
        return new StorageScannerData(radius, exportC, wideview, sortingMode);
    }

    public StorageScannerData withExportC(boolean exportC) {
        return new StorageScannerData(radius, exportC, wideview, sortingMode);
    }

    public StorageScannerData withWideView(boolean wideview) {
        return new StorageScannerData(radius, exportC, wideview, sortingMode);
    }

    public StorageScannerData withSortingMode(SortingMode sortingMode) {
        return new StorageScannerData(radius, exportC, wideview, sortingMode);
    }
}
