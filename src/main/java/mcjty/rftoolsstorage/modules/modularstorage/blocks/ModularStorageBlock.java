package mcjty.rftoolsstorage.modules.modularstorage.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolsstorage.compat.RFToolsStorageTOPDriver;
import mcjty.rftoolsstorage.modules.modularstorage.ModularTypeModule;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;


public class ModularStorageBlock extends BaseBlock {

    public static final EnumProperty<ModularTypeModule> TYPEMODULE = EnumProperty.create("type", ModularTypeModule.class);
    public static final EnumProperty<ModularAmountOverlay> AMOUNT = EnumProperty.create("amount", ModularAmountOverlay.class);

    public ModularStorageBlock() {
        super(new BlockBuilder()
                .topDriver(RFToolsStorageTOPDriver.DRIVER)
                .tileEntitySupplier(ModularStorageTileEntity::new)
                .manualEntry(ManualHelper.create("rftoolsstorage:modularstorage/modularstorage"))
                .info(key("message.rftoolsstorage.shiftmessage"))
                .infoShift(header())
        );
    }


    private static long lastTime = 0;

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TYPEMODULE).add(AMOUNT);
    }

    @Override
    public void setPlacedBy(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        if (placer instanceof Player) {
            // @todo achievements
//            Achievements.trigger((PlayerEntity) placer, Achievements.allTheItems);
        }
    }
}
