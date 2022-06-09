package mcjty.rftoolsstorage.modules.scanner.items;

import mcjty.lib.client.GuiTools;
import mcjty.lib.crafting.INBTPreservingIngredient;
import mcjty.lib.varia.ComponentFactory;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.ModuleTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.api.storage.IStorageScanner;
import mcjty.rftoolsbase.api.various.ITabletSupport;
import mcjty.rftoolsbase.tools.GenericModuleItem;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerConfiguration;
import mcjty.rftoolsstorage.modules.scanner.StorageScannerModule;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerContainer;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

public class StorageControlModuleItem extends GenericModuleItem implements INBTPreservingIngredient, ITabletSupport {

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
                StorageScannerContainer container = StorageScannerContainer.createRemote(id, pos, (StorageScannerTileEntity) te, player);
                te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
                    container.setupInventories(h, inventory);
                });
                return container;
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
        super(new Properties().stacksTo(1).defaultDurability(1).tab(RFToolsStorage.setup.getTab()));
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
    public Class<StorageControlScreenModule> getServerScreenModule() {
        return StorageControlScreenModule.class;
    }

    @Override
    public Class<StorageControlClientScreenModule> getClientScreenModule() {
        return StorageControlClientScreenModule.class;
    }

    @Override
    public String getModuleName() {
        return "Stor";
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .ghostStack("stack0").ghostStack("stack1").ghostStack("stack2").nl()
                .ghostStack("stack3").ghostStack("stack4").ghostStack("stack5").nl()
                .ghostStack("stack6").ghostStack("stack7").ghostStack("stack8").nl()
                .toggle("starred", "Starred", "If enabled only count items", "in 'starred' inventories", "(mark inventories in storage scanner)")
                .block("monitor").nl();
    }

    // @todo 1.14 implement!
    @Override
    public Collection<String> getTagsToPreserve() {
        return Collections.emptyList();
    }
}