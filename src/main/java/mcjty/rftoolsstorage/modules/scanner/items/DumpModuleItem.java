package mcjty.rftoolsstorage.modules.scanner.items;

import com.mojang.serialization.Codec;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.ModuleTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.storage.IStorageScanner;
import mcjty.rftoolsbase.tools.GenericModuleItem;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class DumpModuleItem extends GenericModuleItem {

    public DumpModuleItem() {
        super(RFToolsStorage.setup.defaultProperties().durability(1));
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
    public @Nullable Codec<? extends IScreenModule<?, ?>> codec() {
        return DumpScreenModule.CODEC;
    }

    @Override
    public @Nullable StreamCodec<RegistryFriendlyByteBuf, ? extends IScreenModule<?, ?>> streamCodec() {
        return DumpScreenModule.STREAM_CODEC;
    }

    @Override
    public @Nullable DataComponentType<? extends IScreenModule<?, ?>> componentType() {
        // @todo 1.21 data
        return null;
    }

    @Override
    public IScreenModule<?, ?> createServerScreenModule() {
        return DumpScreenModule.DEFAULT;
    }

    @Override
    public IClientScreenModule<?> createClientScreenModule() {
        return new DumpClientScreenModule();
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
                // @todo 1.21 data
//                guiBuilder.ghostStack("stack" + index);
                index++;
            }
            guiBuilder.nl();
        }
        // @todo 1.21 data
//        guiBuilder
//                .label("Label:").text("text", "Label text").color("color", "Label color").nl()
//                .toggle("matchingTag", "Matching Tag", "If enabled use common tags", "to match items");
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof IStorageScanner) {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            String name = "<invalid>";
            if (!world.getBlockState(pos).isAir()) {
                name = Tools.getReadableName(world, pos);
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
        return InteractionResult.SUCCESS;
    }
}