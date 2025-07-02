package mcjty.rftoolsstorage.modules.craftingmanager;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerBlock;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerContainer;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerTileEntity;
import mcjty.rftoolsstorage.modules.craftingmanager.client.ClientSetup;
import mcjty.rftoolsstorage.modules.craftingmanager.client.GuiCraftingManager;
import mcjty.rftoolsstorage.modules.craftingmanager.system.CraftingDeviceRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import java.util.function.Supplier;

import static mcjty.rftoolsstorage.setup.Registration.*;

public class CraftingManagerModule implements IModule {

    public static final RBlock<BaseBlock, BlockItem, CraftingManagerTileEntity> CRAFTING_MANAGER = RBLOCKS.registerBlock("crafting_manager",
            CraftingManagerTileEntity.class,
            CraftingManagerBlock::new,
            block -> new BlockItem(block.get(), createStandardProperties()),
            CraftingManagerTileEntity::new
    );
    public static final Supplier<MenuType<CraftingManagerContainer>> CONTAINER_CRAFTING_MANAGER = CONTAINERS.register("crafting_manager", GenericContainer::createContainerType);

    public static final CraftingDeviceRegistry CRAFTING_DEVICE_REGISTRY = new CraftingDeviceRegistry();

    public CraftingManagerModule(IEventBus bus, Dist dist) {
        bus.addListener(ClientSetup::modelInit);
        bus.addListener(this::registerMenuScreens);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {
        CRAFTING_DEVICE_REGISTRY.init();
    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
    }

    public void registerMenuScreens(RegisterMenuScreensEvent event) {
        GuiCraftingManager.register(event);
    }

    @Override
    public void initConfig(IEventBus bus) {
    }

    @Override
    public void initDatagen(DataGen dataGen, HolderLookup.Provider provider) {
        dataGen.add(
                Dob.blockBuilder(CRAFTING_MANAGER)
                        .ironPickaxeTags()
                        .blockState(DataGenHelper::createCraftingManager)
        );
    }
}