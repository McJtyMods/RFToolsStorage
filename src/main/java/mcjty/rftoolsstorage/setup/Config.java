package mcjty.rftoolsstorage.setup;


import mcjty.lib.modules.Modules;
import net.neoforged.neoforge.common.ForgeConfigSpec;
import net.neoforged.neoforge.common.ForgeConfigSpec.Builder;
import net.neoforged.neoforge.eventbus.api.IEventBus;
import net.neoforged.neoforge.fml.ModLoadingContext;
import net.neoforged.neoforge.fml.config.ModConfig;

public class Config {

    public static final Builder SERVER_BUILDER = new Builder();
    public static final Builder CLIENT_BUILDER = new Builder();

    public static ForgeConfigSpec SERVER_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;


    public static String CATEGORY_GENERAL = "general";

    public static ForgeConfigSpec.IntValue OVERWORLD_ORE_CHANCES;
    public static ForgeConfigSpec.IntValue OVERWORLD_ORE_VEINSIZE;
    public static ForgeConfigSpec.IntValue OVERWORLD_ORE_MINY;
    public static ForgeConfigSpec.IntValue OVERWORLD_ORE_MAXY;

    public static ForgeConfigSpec.IntValue NETHER_ORE_CHANCES;
    public static ForgeConfigSpec.IntValue NETHER_ORE_VEINSIZE;
    public static ForgeConfigSpec.IntValue NETHER_ORE_MINY;
    public static ForgeConfigSpec.IntValue NETHER_ORE_MAXY;

    public static void register(IEventBus bus, Modules modules) {
        modules.initConfig(bus);

        SERVER_CONFIG = SERVER_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG);
    }
}
