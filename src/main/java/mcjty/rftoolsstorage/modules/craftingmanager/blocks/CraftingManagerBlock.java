package mcjty.rftoolsstorage.modules.craftingmanager.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;

import static mcjty.lib.builder.TooltipBuilder.*;

public class CraftingManagerBlock extends BaseBlock {

    private static final VoxelShape SHAPE = Shapes.box(.1, .1, .4, 1, 1, 1);


    public CraftingManagerBlock() {
        super(new BlockBuilder()
                .properties(Properties.of()
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
    public VoxelShape getOcclusionShape(@Nonnull BlockState state, @Nonnull BlockGetter reader, @Nonnull BlockPos pos) {
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
    public RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.MODEL;
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
