package mcjty.rftoolsstorage.modules.craftingmanager.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nonnull;

import static mcjty.lib.builder.TooltipBuilder.*;

public class CraftingManagerBlock extends BaseBlock {

    public CraftingManagerBlock(Properties properties) {
        super(new BlockBuilder()
                .properties(properties
                        .strength(2.0f)
                        .noOcclusion()
                        .sound(SoundType.GLASS)
                        .isRedstoneConductor((state, world, pos) -> false)
                )
                .info(key("message.rftoolsstorage.shiftmessage"))
                .infoShift(header(), gold())
                .tileEntitySupplier(CraftingManagerTileEntity::new)
        );
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
