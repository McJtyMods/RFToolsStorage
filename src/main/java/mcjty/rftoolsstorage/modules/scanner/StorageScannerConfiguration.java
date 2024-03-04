package mcjty.rftoolsstorage.modules.scanner;

import net.neoforged.neoforge.common.ModConfigSpec;

public class StorageScannerConfiguration {
    public static final String CATEGORY_STORAGE_SCANNER = "storagescanner";
    public static ModConfigSpec.IntValue MAXENERGY;
    public static ModConfigSpec.IntValue RECEIVEPERTICK;
    public static ModConfigSpec.IntValue rfPerRequest;
    public static ModConfigSpec.IntValue rfPerInsert;
    public static ModConfigSpec.IntValue hilightTime;

    public static ModConfigSpec.BooleanValue hilightStarredOnGuiOpen;
    public static ModConfigSpec.BooleanValue requestStraightToInventory;

    public static ModConfigSpec.BooleanValue scannerNoRestrictions;

    public static ModConfigSpec.IntValue STORAGE_CONTROL_RFPERTICK; //6;
    public static ModConfigSpec.IntValue DUMP_RFPERTICK; //0;

    public static ModConfigSpec.BooleanValue xnetRequired;

    public static void init(ModConfigSpec.Builder SERVER_BUILDER, ModConfigSpec.Builder CLIENT_BUILDER) {
        SERVER_BUILDER.comment("Settings for the storage scanner machine").push(CATEGORY_STORAGE_SCANNER);
        CLIENT_BUILDER.comment("Settings for the storage scanner machine").push(CATEGORY_STORAGE_SCANNER);

        rfPerRequest = SERVER_BUILDER
                .comment("Amount of RF used to request an item")
                .defineInRange("rfPerRequest", 100, 0, Integer.MAX_VALUE);
        rfPerInsert = SERVER_BUILDER
                .comment("Amount of RF used to insert an item")
                .defineInRange("rfPerInsert", 20, 0, Integer.MAX_VALUE);
        hilightTime = CLIENT_BUILDER
                .comment("Time (in seconds) to hilight a block in the world")
                .defineInRange("hilightTime", 5, 0, Integer.MAX_VALUE);
        MAXENERGY = SERVER_BUILDER
                .comment("Maximum RF storage that the storage scanner can hold")
                .defineInRange("scannerMaxRF", 50000, 0, Integer.MAX_VALUE);
        RECEIVEPERTICK = SERVER_BUILDER
                .comment("RF per tick that the storage scanner can receive")
                .defineInRange("scannerRFPerTick", 500, 0, Integer.MAX_VALUE);

        STORAGE_CONTROL_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the storage control module")
                .defineInRange("storageControlRFPerTick", 0, 0, Integer.MAX_VALUE);
        DUMP_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the dump module")
                .defineInRange("dumpRFPerTick", 0, 0, Integer.MAX_VALUE);

        hilightStarredOnGuiOpen = CLIENT_BUILDER
                .comment("If this is true then opening the storage scanner GUI will automatically select the starred inventory view")
                .define("hilightStarredOnGuiOpen", true);
        requestStraightToInventory = SERVER_BUILDER
                .comment("If this is true then requesting items from the storage scanner will go straight into the player inventory and not the output slot")
                .define("requestStraightToInventory", true);
        xnetRequired = SERVER_BUILDER
                .comment("If this is true then XNet is required (if present) to be able to connect storages to a storage scanner")
                .define("xnetRequired", false);
        scannerNoRestrictions = SERVER_BUILDER
                .comment("If this is true the scanner will not respect claimed players and not use a fake player to access inventories. The default is false which should make it impossible to scan inventories from other players (if properly claimed)")
                .define("scannerNoRestrictions", false);

        SERVER_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }
}
