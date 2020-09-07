package mcjty.rftoolsstorage.modules.modularstorage.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftoolsbase.tools.ManualHelper;
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
}
