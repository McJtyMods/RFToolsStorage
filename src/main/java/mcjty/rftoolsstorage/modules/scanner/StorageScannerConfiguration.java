package mcjty.rftoolsstorage.modules.scanner;

import net.minecraftforge.common.ForgeConfigSpec;

public class StorageScannerConfiguration {
    public static final String CATEGORY_STORAGE_SCANNER = "storagescanner";
    public static ForgeConfigSpec.IntValue MAXENERGY;
    public static ForgeConfigSpec.IntValue RECEIVEPERTICK;
    public static ForgeConfigSpec.IntValue rfPerRequest;
    public static ForgeConfigSpec.IntValue rfPerInsert;
    public static ForgeConfigSpec.IntValue hilightTime;

    public static ForgeConfigSpec.BooleanValue hilightStarredOnGuiOpen;
    public static ForgeConfigSpec.BooleanValue requestStraightToInventory;

    public static ForgeConfigSpec.BooleanValue xnetRequired;

    public static void init(ForgeConfigSpec.Builder COMMON_BUILDER, ForgeConfigSpec.Builder CLIENT_BUILDER) {
        COMMON_BUILDER.comment("Settings for the storage scanner machine").push(CATEGORY_STORAGE_SCANNER);
        CLIENT_BUILDER.comment("Settings for the storage scanner machine").push(CATEGORY_STORAGE_SCANNER);

        rfPerRequest = COMMON_BUILDER
                .comment("Amount of RF used to request an item")
                .defineInRange("rfPerRequest", 100, 0, Integer.MAX_VALUE);
        rfPerInsert = COMMON_BUILDER
                .comment("Amount of RF used to insert an item")
                .defineInRange("rfPerInsert", 20, 0, Integer.MAX_VALUE);
        hilightTime = CLIENT_BUILDER
                .comment("Time (in seconds) to hilight a block in the world")
                .defineInRange("hilightTime", 5, 0, Integer.MAX_VALUE);
        MAXENERGY = COMMON_BUILDER
                .comment("Maximum RF storage that the storage scanner can hold")
                .defineInRange("scannerMaxRF", 50000, 0, Integer.MAX_VALUE);
        RECEIVEPERTICK = COMMON_BUILDER
                .comment("RF per tick that the storage scanner can receive")
                .defineInRange("scannerRFPerTick", 500, 0, Integer.MAX_VALUE);

        hilightStarredOnGuiOpen = CLIENT_BUILDER
                .comment("If this is true then opening the storage scanner GUI will automatically select the starred inventory view")
                .define("hilightStarredOnGuiOpen", true);
        requestStraightToInventory = COMMON_BUILDER
                .comment("If this is true then requesting items from the storage scanner will go straight into the player inventory and not the output slot")
                .define("requestStraightToInventory", true);
        xnetRequired = COMMON_BUILDER
                .comment("If this is true then XNet is required (if present) to be able to connect storages to a storage scanner")
                .define("xnetRequired", false);

        COMMON_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }
}
