package mcjty.rftoolsstorage.modules.craftingmanager.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import static mcjty.lib.builder.TooltipBuilder.*;


public class CraftingManagerBlock extends BaseBlock {

    private static final VoxelShape SHAPE = VoxelShapes.create(.1, .1, .4, 1, 1, 1);


    public CraftingManagerBlock() {
        super(new BlockBuilder()
                .properties(Properties.create(Material.GLASS)
                        .hardnessAndResistance(2.0f)
                        .sound(SoundType.GLASS)
                        .setOpaque((state, world, pos) -> false)
                )
                .info(key("message.rftoolsstorage.shiftmessage"))
                .infoShift(header(), gold())
                .tileEntitySupplier(CraftingManagerTileEntity::new)
        );
    }

    @Override
    public VoxelShape getRenderShape(BlockState p_196247_1_, IBlockReader p_196247_2_, BlockPos p_196247_3_) {
        return SHAPE;
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }

    // @todo temporary
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
        if (side == Direction.UP || side == Direction.DOWN) {
            return adjacentBlockState.getBlock() == this ? true : super.isSideInvisible(state, adjacentBlockState, side);
        }
        return false;
    }
}
