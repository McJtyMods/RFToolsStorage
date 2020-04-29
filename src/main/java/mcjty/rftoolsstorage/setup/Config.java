package mcjty.rftoolsstorage.setup;


import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageConfiguration;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerConfiguration;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;

import java.nio.file.Path;

public class Config {

    private static final Builder SERVER_BUILDER = new Builder();
    private static final Builder CLIENT_BUILDER = new Builder();

    public static final ForgeConfigSpec SERVER_CONFIG;
    public static final ForgeConfigSpec CLIENT_CONFIG;


    public static String CATEGORY_GENERAL = "general";

    public static ForgeConfigSpec.IntValue OVERWORLD_ORE_CHANCES;
    public static ForgeConfigSpec.IntValue OVERWORLD_ORE_VEINSIZE;
    public static ForgeConfigSpec.IntValue OVERWORLD_ORE_MINY;
    public static ForgeConfigSpec.IntValue OVERWORLD_ORE_MAXY;

    public static ForgeConfigSpec.IntValue NETHER_ORE_CHANCES;
    public static ForgeConfigSpec.IntValue NETHER_ORE_VEINSIZE;
    public static ForgeConfigSpec.IntValue NETHER_ORE_MINY;
    public static ForgeConfigSpec.IntValue NETHER_ORE_MAXY;

    public static void loadConfig(ForgeConfigSpec spec, Path path) {

        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        configData.load();
        spec.setConfig(configData);
    }

    static {
        ModularStorageConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);
        StorageScannerConfiguration.init(SERVER_BUILDER, CLIENT_BUILDER);

        SERVER_CONFIG = SERVER_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }
}
