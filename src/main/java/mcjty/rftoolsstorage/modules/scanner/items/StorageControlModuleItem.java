package mcjty.rftoolsstorage.modules.scanner.items;

import mcjty.lib.client.GuiTools;
import mcjty.lib.crafting.INBTPreservingIngredient;
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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

public class StorageControlModuleItem extends GenericModuleItem implements INBTPreservingIngredient, ITabletSupport {

    @Override
    public Item getInstalledTablet() {
        return StorageScannerModule.TABLET_SCANNER.get();
    }

    @Override
    public void openGui(@Nonnull PlayerEntity player, @Nonnull ItemStack tabletItem, @Nonnull ItemStack containingItem) {
        BlockPos pos = ModuleTools.getPositionFromModule(containingItem);
        RegistryKey<World> dimensionType = ModuleTools.getDimensionFromModule(containingItem);
        GuiTools.openRemoteGui(player, dimensionType, pos, te -> new INamedContainerProvider() {
            @Nonnull
            @Override
            public ITextComponent getDisplayName() {
                return new StringTextComponent("Remote Storage Scanner");
            }

            @Nonnull
            @Override
            public Container createMenu(int id, @Nonnull PlayerInventory inventory, @Nonnull PlayerEntity player) {
                StorageScannerContainer container = StorageScannerContainer.createRemote(id, pos, (StorageScannerTileEntity) te);
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
    public ActionResultType useOn(ItemUseContext context) {
        ItemStack stack = context.getItemInHand();
        World world = context.getLevel();
        PlayerEntity player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof IStorageScanner) {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            String name = "<invalid>";
            if (block != null && !block.isAir(state, world, pos)) {
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
        return ActionResultType.SUCCESS;
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