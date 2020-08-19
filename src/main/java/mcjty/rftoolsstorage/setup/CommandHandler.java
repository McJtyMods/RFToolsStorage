package mcjty.rftoolsstorage.setup;

import mcjty.lib.McJtyLib;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.DimensionId;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.craftinggrid.StorageCraftingTools;
import mcjty.rftoolsstorage.modules.scanner.tools.StorageScannerTools;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

public class CommandHandler {

    public static final String CMD_COMPACT = "compact";
    public static final String CMD_CLEAR_GRID = "clearGrid";
    public static final String CMD_CYCLE_STORAGE = "cycleStorage";

    public static final String CMD_REQUEST_SCANNER_CONTENTS = "requestScannerContents";
    public static final String CMD_SCANNER_SEARCH = "scannerSearch";
    public static final Key<Integer> PARAM_SCANNER_DIM = new Key<>("scannerdim", Type.INTEGER);
    public static final Key<BlockPos> PARAM_SCANNER_POS = new Key<>("scannerpos", Type.BLOCKPOS);
    public static final Key<BlockPos> PARAM_INV_POS = new Key<>("invpos", Type.BLOCKPOS);
    public static final Key<String> PARAM_SEARCH_TEXT = new Key<>("text", Type.STRING);

    public static final String CMD_REQUEST_STORAGE_INFO = "requestStorageInfo";
//    public static final Key<Integer> PARAM_DIMENSION = new Key<>("dimension", Type.INTEGER);

    public static final String CMD_CLEAR_TARGET = "clearTarget";
    public static final String CMD_SET_TARGET = "setTarget";
    public static final Key<Integer> PARAM_TARGET = new Key<>("target", Type.INTEGER);

    public static final String CMD_GET_TARGETS = "getTargets";
    public static final String CMD_FORCE_TELEPORT = "forceTeleport";

    public static final String CMD_CYCLE_DESTINATION = "cycleDestination";
    public static final Key<Boolean> PARAM_NEXT = new Key<>("next", Type.BOOLEAN);

    public static final String CMD_GET_DESTINATION_INFO = "getDestinationInfo";
    public static final String CMD_GET_RF_IN_RANGE = "getRfInRange";
    public static final String CMD_REQUEST_SHAPE_DATA = "requestShapeData";
    public static final String CMD_REQUEST_SCAN_DIRTY = "requestScanDirty";
    public static final String CMD_REQUEST_LOCATOR_ENERGY = "requestLocatorEnergy";

    public static final String CMD_GET_SECURITY_INFO = "getSecurityInfo";
    public static final Key<Integer> PARAM_ID = new Key<>("id", Type.INTEGER);

    public static final String CMD_GET_SECURITY_NAME = "getSecurityName";

    public static final String CMD_CRAFT_FROM_GRID = "craftFromGrid";
    public static final Key<Integer> PARAM_COUNT = new Key<>("count", Type.INTEGER);
    public static final Key<Boolean> PARAM_TEST = new Key<>("test", Type.BOOLEAN);

    public static final String CMD_REQUEST_GRID_SYNC = "requestGridSync";
    public static final Key<BlockPos> PARAM_POS = new Key<>("pos", Type.BLOCKPOS);
    public static final Key<DimensionId> PARAM_DIMENSION = new Key<>("dimension", Type.DIMENSION_TYPE);

    public static final String CMD_GET_COUNTER_INFO = "getCounterInfo";

    public static void registerCommands() {
        McJtyLib.registerCommand(RFToolsStorage.MODID, CMD_REQUEST_SCANNER_CONTENTS, (player, arguments) -> {
            StorageScannerTools.requestContents(player, DimensionType.getById(arguments.get(PARAM_SCANNER_DIM)), arguments.get(PARAM_SCANNER_POS), arguments.get(PARAM_INV_POS));
            return true;
        });
        McJtyLib.registerCommand(RFToolsStorage.MODID, CMD_SCANNER_SEARCH, (player, arguments) -> {
            StorageScannerTools.scannerSearch(player, DimensionType.getById(arguments.get(PARAM_SCANNER_DIM)), arguments.get(PARAM_SCANNER_POS), arguments.get(PARAM_SEARCH_TEXT));
            return true;
        });

        McJtyLib.registerCommand(RFToolsStorage.MODID, CMD_COMPACT, (player, arguments) -> {
            // @todo 1.14
//            StorageTools.compact(player);
            return true;
        });
        McJtyLib.registerCommand(RFToolsStorage.MODID, CMD_CLEAR_GRID, (player, arguments) -> {
            // @todo 1.14
//            StorageTools.clearGrid(player);
            return true;
        });
        McJtyLib.registerCommand(RFToolsStorage.MODID, CMD_CYCLE_STORAGE, (player, arguments) -> {
            // @todo 1.14
//            StorageTools.cycleStorage(player);
            return true;
        });


        McJtyLib.registerCommand(RFToolsStorage.MODID, CMD_CRAFT_FROM_GRID, (player, arguments) -> {
            StorageCraftingTools.craftFromGrid(player, arguments.get(PARAM_COUNT), arguments.get(PARAM_TEST),
                    arguments.get(PARAM_POS), arguments.get(PARAM_DIMENSION));
            return true;
        });
        McJtyLib.registerCommand(RFToolsStorage.MODID, CMD_REQUEST_GRID_SYNC, (player, arguments) -> {
            StorageCraftingTools.requestGridSync(player, arguments.get(PARAM_POS), arguments.get(PARAM_DIMENSION));
            return true;
        });
    }

}
