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


import net.minecraft.block.AbstractBlock.Properties;

import javax.annotation.Nonnull;

public class CraftingManagerBlock extends BaseBlock {

    private static final VoxelShape SHAPE = VoxelShapes.box(.1, .1, .4, 1, 1, 1);


    public CraftingManagerBlock() {
        super(new BlockBuilder()
                .properties(Properties.of(Material.GLASS)
                        .strength(2.0f)
                        .sound(SoundType.GLASS)
                        .isRedstoneConductor((state, world, pos) -> false)
                )
                .info(key("message.rftoolsstorage.shiftmessage"))
                .infoShift(header(), gold())
                .tileEntitySupplier(CraftingManagerTileEntity::new)
        );
    }

    @Override
    @Nonnull
    public VoxelShape getOcclusionShape(@Nonnull BlockState state, @Nonnull IBlockReader reader, @Nonnull BlockPos pos) {
        return SHAPE;
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }

    // @todo temporary
    @SuppressWarnings("deprecation")
    @Override
    @Nonnull
    public BlockRenderType getRenderShape(@Nonnull BlockState state) {
        return BlockRenderType.MODEL;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean skipRendering(@Nonnull BlockState state, @Nonnull BlockState adjacentBlockState, @Nonnull Direction side) {
        if (side == Direction.UP || side == Direction.DOWN) {
            return adjacentBlockState.getBlock() == this ? true : super.skipRendering(state, adjacentBlockState, side);
        }
        return false;
    }
}
