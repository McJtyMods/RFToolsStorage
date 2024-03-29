package mcjty.rftoolsstorage.modules.modularstorage;

import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum ModularTypeModule implements IStringSerializable {
    TYPE_NONE("none"),
    TYPE_GENERIC("generic"),
    TYPE_ORE("ore");

    private final String name;

    ModularTypeModule(String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public String getSerializedName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
