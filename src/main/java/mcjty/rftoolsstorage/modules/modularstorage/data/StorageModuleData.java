package mcjty.rftoolsstorage.modules.modularstorage.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.CompositeStreamCodec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;
import java.util.UUID;

public record StorageModuleData(int id, int version, int infoAmount, UUID uuid,
                                long creationTime, long updateTime, String createdBy) {

    public static final StorageModuleData DEFAULT = new StorageModuleData(-1, 0, -1, null, -1, -1, null);

    public static final Codec<StorageModuleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("id").forGetter(StorageModuleData::id),
            Codec.INT.fieldOf("version").forGetter(StorageModuleData::version),
            Codec.INT.fieldOf("infoAmount").forGetter(StorageModuleData::infoAmount),
            UUIDUtil.CODEC.optionalFieldOf("uuid").forGetter(s -> Optional.ofNullable(s.uuid)),
            Codec.LONG.fieldOf("creation").forGetter(StorageModuleData::creationTime),
            Codec.LONG.fieldOf("update").forGetter(StorageModuleData::updateTime),
            Codec.STRING.fieldOf("createdBy").forGetter(StorageModuleData::createdBy)
    ).apply(instance, (id, vs, ia, uuid, creation, update, cb) -> new StorageModuleData(id, vs, ia, uuid.orElse(null), creation, update, cb)));

    public static final StreamCodec<RegistryFriendlyByteBuf, StorageModuleData> STREAM_CODEC = CompositeStreamCodec.composite(
            ByteBufCodecs.INT, StorageModuleData::id,
            ByteBufCodecs.INT, StorageModuleData::version,
            ByteBufCodecs.INT, StorageModuleData::infoAmount,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), s -> Optional.ofNullable(s.uuid),
            ByteBufCodecs.VAR_LONG, StorageModuleData::creationTime,
            ByteBufCodecs.VAR_LONG, StorageModuleData::updateTime,
            ByteBufCodecs.STRING_UTF8, StorageModuleData::createdBy,
            (id, vs, ia, uuid, creation, update, cb) -> new StorageModuleData(id, vs, ia, uuid.orElse(null), creation, update, cb)
    );

    public StorageModuleData withId(int id) {
        return new StorageModuleData(id, version, infoAmount, uuid, creationTime, updateTime, createdBy);
    }

    public StorageModuleData withUuid(UUID uuid) {
        return new StorageModuleData(id, version, infoAmount, uuid, creationTime, updateTime, createdBy);
    }

    public StorageModuleData withVersion(int version) {
        return new StorageModuleData(id, version, infoAmount, uuid, creationTime, updateTime, createdBy);
    }

    public StorageModuleData withInfoAmount(int infoAmount) {
        return new StorageModuleData(id, version, infoAmount, uuid, creationTime, updateTime, createdBy);
    }

    public StorageModuleData withCreationTime(long creationTime) {
        return new StorageModuleData(id, version, infoAmount, uuid, creationTime, updateTime, createdBy);
    }

    public StorageModuleData withUpdateTime(long updateTime) {
        return new StorageModuleData(id, version, infoAmount, uuid, creationTime, updateTime, createdBy);
    }

    public StorageModuleData withCreatedBy(String createdBy) {
        return new StorageModuleData(id, version, infoAmount, uuid, creationTime, updateTime, createdBy);
    }
}
