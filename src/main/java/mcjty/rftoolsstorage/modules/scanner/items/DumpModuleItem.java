package mcjty.rftoolsstorage.modules.scanner.items;

import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.api.storage.IStorageScanner;
import mcjty.rftoolsbase.tools.GenericModuleItem;
import mcjty.lib.varia.ModuleTools;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerConfiguration;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DumpModuleItem extends GenericModuleItem {

    public DumpModuleItem() {
        super(new Properties().defaultDurability(1).tab(RFToolsStorage.setup.getTab()));
    }

    @Override
    protected int getUses(ItemStack stack) {
        return StorageScannerConfiguration.DUMP_RFPERTICK.get();
    }

    @Override
    protected boolean hasGoldMessage(ItemStack stack) {
        return !ModuleTools.hasModuleTarget(stack);
    }

    @Override
    protected String getInfoString(ItemStack stack) {
        return ModuleTools.getTargetString(stack);
    }


    //    @Override
//    public int getMaxItemUseDuration(ItemStack stack) {
//        return 1;
//    }

    @Override
    public Class<DumpScreenModule> getServerScreenModule() {
        return DumpScreenModule.class;
    }

    @Override
    public Class<DumpClientScreenModule> getClientScreenModule() {
        return DumpClientScreenModule.class;
    }

    @Override
    public String getModuleName() {
        return "Dump";
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        int index = 0;
        for (int y = 0 ; y < DumpScreenModule.ROWS ; y++) {
            for (int x = 0 ; x < DumpScreenModule.COLS ; x++) {
                guiBuilder.ghostStack("stack" + index);
                index++;
            }
            guiBuilder.nl();
        }
        guiBuilder
                .label("Label:").text("text", "Label text").color("color", "Label color").nl()
                .toggle("matchingTag", "Matching Tag", "If enabled use common tags", "to match items");
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        ItemStack stack = context.getItemInHand();
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof IStorageScanner) {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            String name = "<invalid>";
            if (block != null && !block.isAir(state, world, pos)) {
                name = BlockTools.getReadableName(world, pos);
            }
            ModuleTools.setPositionInModule(stack, world.dimension(), pos, name);
            if (world.isClientSide) {
                Logging.message(context.getPlayer(), "Storage module is set to block '" + name + "'");
            }
        } else {
            ModuleTools.clearPositionInModule(stack);
            if (world.isClientSide) {
                Logging.message(context.getPlayer(), "Storage module is cleared");
            }
        }
        return ActionResultType.SUCCESS;
    }
}