package mcjty.rftoolsstorage.setup;


import mcjty.lib.modules.Modules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;

public class Config {

    public static final Builder SERVER_BUILDER = new Builder();
    public static final Builder CLIENT_BUILDER = new Builder();

    public static ModConfigSpec SERVER_CONFIG;
    public static ModConfigSpec CLIENT_CONFIG;


    public static String CATEGORY_GENERAL = "general";

    public static ModConfigSpec.IntValue OVERWORLD_ORE_CHANCES;
    public static ModConfigSpec.IntValue OVERWORLD_ORE_VEINSIZE;
    public static ModConfigSpec.IntValue OVERWORLD_ORE_MINY;
    public static ModConfigSpec.IntValue OVERWORLD_ORE_MAXY;

    public static ModConfigSpec.IntValue NETHER_ORE_CHANCES;
    public static ModConfigSpec.IntValue NETHER_ORE_VEINSIZE;
    public static ModConfigSpec.IntValue NETHER_ORE_MINY;
    public static ModConfigSpec.IntValue NETHER_ORE_MAXY;

    public static void register(ModContainer container, IEventBus bus, Modules modules) {
        modules.initConfig(bus);

        SERVER_CONFIG = SERVER_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();

        container.registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG);
        container.registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG);
    }
}
