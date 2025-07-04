package mcjty.rftoolsstorage.modules.scanner.items;

import com.mojang.serialization.Codec;
import mcjty.lib.client.GuiTools;
import mcjty.lib.crafting.IComponentsToPreserve;
import mcjty.lib.varia.ComponentFactory;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.ModuleTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.storage.IStorageScanner;
import mcjty.rftoolsbase.api.various.ITabletSupport;
import mcjty.rftoolsbase.tools.GenericModuleItem;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerConfiguration;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerModule;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerContainer;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class StorageControlModuleItem extends GenericModuleItem implements IComponentsToPreserve, ITabletSupport {

    @Override
    public Item getInstalledTablet() {
        return StorageScannerModule.TABLET_SCANNER.get();
    }

    @Override
    public void openGui(@Nonnull Player player, @Nonnull ItemStack tabletItem, @Nonnull ItemStack containingItem) {
        BlockPos pos = ModuleTools.getPositionFromModule(containingItem);
        ResourceKey<Level> dimensionType = ModuleTools.getDimensionFromModule(containingItem);
        GuiTools.openRemoteGui(player, dimensionType, pos, te -> new MenuProvider() {
            @Nonnull
            @Override
            public Component getDisplayName() {
                return ComponentFactory.literal("Remote Storage Scanner");
            }

            @Nonnull
            @Override
            public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inventory, @Nonnull Player player) {
                if (te instanceof StorageScannerTileEntity scanner) {
                    StorageScannerContainer container = StorageScannerContainer.createRemote(id, pos, scanner, player);
                    container.setupInventories(scanner.getItems(), inventory);
                    return container;
                } else {
                    Logging.logError("Cannot open remote storage scanner GUI at " + pos + " because the tile entity is not a StorageScannerTileEntity!");
                    return null;
                }
            }
        });
    }

    @Override
    protected int getUses(ItemStack stack) {
        return StorageScannerConfiguration.STORAGE_CONTROL_RFPERTICK.get();
    }

    @Override
    protected boolean hasGoldMessage(ItemStack stack) {
        return !ModuleTools.hasModuleTarget(stack);
    }

    @Override
    protected String getInfoString(ItemStack stack) {
        return ModuleTools.getTargetString(stack);
    }

    public StorageControlModuleItem() {
        super(RFToolsStorage.setup.defaultProperties().stacksTo(1).durability(1));
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Level world = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof IStorageScanner) {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            String name = "<invalid>";
            if (block != null && !world.getBlockState(pos).isAir()) {
                name = Tools.getReadableName(world, pos);
            }
            ModuleTools.setPositionInModule(stack, world.dimension(), pos, name);
            if (world.isClientSide) {
                Logging.message(player, "Storage module is set to block '" + name + "'");
            }
        } else {
            ModuleTools.clearPositionInModule(stack);
            if (world.isClientSide) {
                Logging.message(player, "Storage module is cleared");
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable Codec<? extends IScreenModule<?, ?>> codec() {
        return StorageControlScreenModule.CODEC;
    }

    @Override
    public @Nullable StreamCodec<RegistryFriendlyByteBuf, ? extends IScreenModule<?, ?>> streamCodec() {
        return StorageControlScreenModule.STREAM_CODEC;
    }

    @Override
    public @Nullable DataComponentType<? extends IScreenModule<?, ?>> componentType() {
        return StorageScannerModule.MODULE_CONTROL_DATA.get();
    }

    @Override
    public IScreenModule<?, ?> createServerScreenModule() {
        return StorageControlScreenModule.DEFAULT;
    }

    @Override
    public IClientScreenModule<?> createClientScreenModule() {
        return new StorageControlClientScreenModule();
    }

    @Override
    public String getModuleName() {
        return "Stor";
    }

    public static StorageControlScreenModule data(ItemStack stack) {
        StorageControlScreenModule data = stack.get(StorageScannerModule.MODULE_CONTROL_DATA);
        if (data == null) {
            data = StorageControlScreenModule.DEFAULT;
        }
        return data;
    }

    public static void data(ItemStack stack, Function<StorageControlScreenModule, StorageControlScreenModule> setter) {
        StorageControlScreenModule data = data(stack);
        data = setter.apply(data);
        stack.set(StorageScannerModule.MODULE_CONTROL_DATA, data);
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .ghostStack((module, stack) -> data(module, d -> d.withStack(0, stack)), module -> data(module).stacks().get(0))
                .ghostStack((module, stack) -> data(module, d -> d.withStack(1, stack)), module -> data(module).stacks().get(1))
                .ghostStack((module, stack) -> data(module, d -> d.withStack(2, stack)), module -> data(module).stacks().get(2))
                .nl()

                .ghostStack((module, stack) -> data(module, d -> d.withStack(3, stack)), module -> data(module).stacks().get(3))
                .ghostStack((module, stack) -> data(module, d -> d.withStack(4, stack)), module -> data(module).stacks().get(4))
                .ghostStack((module, stack) -> data(module, d -> d.withStack(5, stack)), module -> data(module).stacks().get(5))
                .nl()

                .ghostStack((module, stack) -> data(module, d -> d.withStack(6, stack)), module -> data(module).stacks().get(6))
                .ghostStack((module, stack) -> data(module, d -> d.withStack(7, stack)), module -> data(module).stacks().get(7))
                .ghostStack((module, stack) -> data(module, d -> d.withStack(8, stack)), module -> data(module).stacks().get(8))
                .nl()

                .toggle((module, starred) -> data(module, d -> d.withStarred(starred)), module -> data(module).starred(), "Starred", "If enabled only count items", "in 'starred' inventories", "(mark inventories in storage scanner)")
                .block(module -> data(module).pos(), module -> "").nl();
    }

    @Override
    public Collection<DataComponentType<?>> getComponentsToPreserve() {
        return List.of(StorageScannerModule.MODULE_CONTROL_DATA.get());
    }
}