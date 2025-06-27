package mcjty.rftoolsstorage.modules.modularstorage.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ModularStorageData(boolean locked) {

    public static final Codec<ModularStorageData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("locked").forGetter(ModularStorageData::locked)
    ).apply(instance, ModularStorageData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ModularStorageData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ModularStorageData::locked,
            ModularStorageData::new
    );

    public ModularStorageData withLocked(boolean locked) {
        return new ModularStorageData(locked);
    }
}
