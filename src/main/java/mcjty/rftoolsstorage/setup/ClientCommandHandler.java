package mcjty.rftoolsstorage.setup;

import mcjty.lib.McJtyLib;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.modularstorage.blocks.ModularStorageBlock;
import mcjty.rftoolsstorage.modules.scanner.client.GuiStorageScanner;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.List;

public class ClientCommandHandler {

    public static final String CMD_RETURN_SCANNER_CONTENTS = "returnScannerContents";
    public static final Key<List<ItemStack>> PARAM_STACKS = new Key<>("stacks", Type.ITEMSTACK_LIST);

    public static final String CMD_RETURN_SCANNER_SEARCH = "returnScannerSearch";
    public static final Key<List<BlockPos>> PARAM_INVENTORIES = new Key<>("inventories", Type.POS_LIST);

    public static final Key<String> PARAM_NAME = new Key<>("name", Type.STRING);
    public static final Key<Integer> PARAM_COUNTER = new Key<>("counter", Type.INTEGER);

    public static final String CMD_RETURN_STORAGE_INFO = "returnStorageInfo";

    public static final String CMD_POSITION_TO_CLIENT = "positionToClient";
    public static final Key<BlockPos> PARAM_POS = new Key<>("pos", Type.BLOCKPOS);
    public static final Key<BlockPos> PARAM_SCAN = new Key<>("scan", Type.BLOCKPOS);

    public static void registerCommands() {
        McJtyLib.registerClientCommand(RFToolsStorage.MODID, CMD_RETURN_SCANNER_CONTENTS, (player, arguments) -> {
            GuiStorageScanner.fromServer_inventory = arguments.get(PARAM_STACKS);
            return true;
        });
        McJtyLib.registerClientCommand(RFToolsStorage.MODID, CMD_RETURN_SCANNER_SEARCH, (player, arguments) -> {
            GuiStorageScanner.fromServer_foundInventories = new HashSet<>(arguments.get(PARAM_INVENTORIES));
            return true;
        });
        McJtyLib.registerClientCommand(RFToolsStorage.MODID, CMD_RETURN_STORAGE_INFO, (player, arguments) -> {
            ModularStorageBlock.cntReceived = arguments.get(PARAM_COUNTER);
            ModularStorageBlock.nameModuleReceived = arguments.get(PARAM_NAME);
            return true;
        });
    }
}
