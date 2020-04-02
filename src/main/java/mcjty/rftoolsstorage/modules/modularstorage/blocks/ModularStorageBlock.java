package mcjty.rftoolsstorage.modules.modularstorage.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftoolsstorage.compat.RFToolsStorageTOPDriver;
import mcjty.rftoolsstorage.modules.modularstorage.ModularTypeModule;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;


public class ModularStorageBlock extends BaseBlock {

    public static final EnumProperty<ModularTypeModule> TYPEMODULE = EnumProperty.create("type", ModularTypeModule.class);
    public static final EnumProperty<ModularAmountOverlay> AMOUNT = EnumProperty.create("amount", ModularAmountOverlay.class);

    // Clientside
    public static int cntReceived = 1;
    public static String nameModuleReceived = "";

    public ModularStorageBlock() {
        super(new BlockBuilder()
                .topDriver(RFToolsStorageTOPDriver.DRIVER)
                .tileEntitySupplier(ModularStorageTileEntity::new)
                .info(key("message.rftoolsstorage.shiftmessage"))
                .infoShift(header())
        );
    }


    private static long lastTime = 0;

// @todo 1.14
//    @Override
//    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
//        TileEntity tileEntity = world instanceof ChunkCache ? ((ChunkCache) world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) : world.getTileEntity(pos);
//        if (tileEntity instanceof ModularStorageTileEntity) {
//            ModularStorageTileEntity te = (ModularStorageTileEntity) tileEntity;
//            ItemStack stack = te.getInventoryHelper().getStackInSlot(ModularStorageContainer.SLOT_TYPE_MODULE);
//
//            int level = te.getRenderLevel();
//            int remoteId = te.getRemoteId();
//
//            ModularAmountOverlay p = AMOUNT_NONE;
//            if (remoteId > 0) {
//                switch (level) {
//                    case -1:
//                        p = AMOUNT_NONE;
//                        break;
//                    case 0:
//                        p = AMOUNT_R0;
//                        break;
//                    case 1:
//                        p = AMOUNT_R1;
//                        break;
//                    case 2:
//                        p = AMOUNT_R2;
//                        break;
//                    case 3:
//                        p = AMOUNT_R3;
//                        break;
//                    case 4:
//                        p = AMOUNT_R4;
//                        break;
//                    case 5:
//                        p = AMOUNT_R5;
//                        break;
//                    case 6:
//                        p = AMOUNT_R6;
//                        break;
//                    case 7:
//                        p = AMOUNT_R7;
//                        break;
//                }
//            } else {
//                switch (level) {
//                    case -1:
//                        p = AMOUNT_NONE;
//                        break;
//                    case 0:
//                        p = AMOUNT_G0;
//                        break;
//                    case 1:
//                        p = AMOUNT_G1;
//                        break;
//                    case 2:
//                        p = AMOUNT_G2;
//                        break;
//                    case 3:
//                        p = AMOUNT_G3;
//                        break;
//                    case 4:
//                        p = AMOUNT_G4;
//                        break;
//                    case 5:
//                        p = AMOUNT_G5;
//                        break;
//                    case 6:
//                        p = AMOUNT_G6;
//                        break;
//                    case 7:
//                        p = AMOUNT_G7;
//                        break;
//                }
//            }
//
//            IBlockState newstate = state.withProperty(AMOUNT, p);
//
//            if (stack.isEmpty()) {
//                return newstate.withProperty(TYPEMODULE, TYPE_NONE);
//            } else if (stack.getItem() == ModularStorageSetup.genericTypeItem) {
//                return newstate.withProperty(TYPEMODULE, TYPE_GENERIC);
//            } else if (stack.getItem() == ModularStorageSetup.oreDictTypeItem) {
//                return newstate.withProperty(TYPEMODULE, TYPE_ORE);
//            }
//            return newstate.withProperty(TYPEMODULE, TYPE_NONE);
//        } else {
//            return super.getActualState(state, world, pos);
//        }
//    }


//    @Override
//    protected IModuleSupport getModuleSupport() {
//        return new ModuleSupport(ModularStorageContainer.SLOT_FILTER_MODULE) {
//            @Override
//            public boolean isModule(ItemStack itemStack) {
//                return itemStack.getItem() == ModularStorageSetup.storageFilterItem;
//            }
//        };
//    }


    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(TYPEMODULE).add(AMOUNT);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (placer instanceof PlayerEntity) {
            // @todo achievements
//            Achievements.trigger((PlayerEntity) placer, Achievements.allTheItems);
        }
    }

    // @todo 1.14
//    @Override
//    public boolean hasComparatorInputOverride(IBlockState state) {
//        return true;
//    }
//
//    @Override
//    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
//        TileEntity te = worldIn.getTileEntity(pos);
//        if (te instanceof ModularStorageTileEntity) {
//            ModularStorageTileEntity mste = (ModularStorageTileEntity) te;
//            return MathHelper.floor(((float) mste.getNumStacks() / mste.getMaxSize()) * 14.0F) + (mste.getNumStacks() > 0 ? 1 : 0);
//        }
//        return 0;
//    }
}
