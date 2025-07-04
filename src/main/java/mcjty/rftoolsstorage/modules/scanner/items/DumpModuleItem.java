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
import mcjty.rftoolsstorage.modules.scanner.StorageScannerModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

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
        return StorageScannerModule.MODULE_DUMP_DATA.get();
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

    public static DumpScreenModule data(ItemStack stack) {
        DumpScreenModule data = stack.get(StorageScannerModule.MODULE_DUMP_DATA);
        if (data == null) {
            data = DumpScreenModule.DEFAULT;
        }
        return data;
    }

    public static void data(ItemStack stack, Function<DumpScreenModule, DumpScreenModule> setter) {
        DumpScreenModule data = data(stack);
        data = setter.apply(data);
        stack.set(StorageScannerModule.MODULE_DUMP_DATA, data);
    }


    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        AtomicInteger index = new AtomicInteger(0);
        for (int y = 0 ; y < DumpScreenModule.ROWS ; y++) {
            for (int x = 0 ; x < DumpScreenModule.COLS ; x++) {
                guiBuilder.ghostStack(
                        (module, stack) -> data(module, d -> d.withStack(index.get(), stack)),
                        module -> data(module).stacks().get(index.get()));
                index.addAndGet(1);
            }
            guiBuilder.nl();
        }
        guiBuilder
                .label("Label:")
                .text((module, text) -> data(module, d -> d.withLine(text)), module -> data(module).line(), "Label text")
                .color((module, color) -> data(module, d -> d.withColor(color)), module -> data(module).color(), "Label color").nl()
                .toggle((module, tag) -> data(module, d -> d.withMatchingTag(tag)), module -> data(module).matchingTag(), "Matching Tag", "If enabled use common tags", "to match items");
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
            String name = "<invalid>";
            if (!state.isAir()) {
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