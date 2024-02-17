package mcjty.rftoolsstorage.modules.craftingmanager;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.lib.setup.DeferredBlock;
import mcjty.lib.setup.DeferredItem;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerBlock;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerContainer;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerTileEntity;
import mcjty.rftoolsstorage.modules.craftingmanager.client.ClientSetup;
import mcjty.rftoolsstorage.modules.craftingmanager.client.GuiCraftingManager;
import mcjty.rftoolsstorage.modules.craftingmanager.system.CraftingDeviceRegistry;
import mcjty.rftoolsstorage.setup.Registration;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.function.Supplier;

import static mcjty.rftoolsstorage.RFToolsStorage.tab;
import static mcjty.rftoolsstorage.setup.Registration.*;

public class CraftingManagerModule implements IModule {

    public static final DeferredBlock<Block> CRAFTING_MANAGER = BLOCKS.register("crafting_manager", CraftingManagerBlock::new);
    public static final DeferredItem<Item> CRAFTING_MANAGER_ITEM = ITEMS.register("crafting_manager", tab(() -> new BlockItem(CRAFTING_MANAGER.get(), Registration.createStandardProperties())));
    public static final Supplier<BlockEntityType<CraftingManagerTileEntity>> TYPE_CRAFTING_MANAGER = TILES.register("crafting_manager", () -> BlockEntityType.Builder.of(CraftingManagerTileEntity::new, CRAFTING_MANAGER.get()).build(null));
    public static final Supplier<MenuType<CraftingManagerContainer>> CONTAINER_CRAFTING_MANAGER = CONTAINERS.register("crafting_manager", GenericContainer::createContainerType);

    public static final CraftingDeviceRegistry CRAFTING_DEVICE_REGISTRY = new CraftingDeviceRegistry();

    public CraftingManagerModule(IEventBus bus, Dist dist) {
        bus.addListener(ClientSetup::modelInit);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {
        CRAFTING_DEVICE_REGISTRY.init();
    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GuiCraftingManager.register();
        });
    }

    @Override
    public void initConfig(IEventBus bus) {
    }

    @Override
    public void initDatagen(DataGen dataGen) {
        dataGen.add(
                Dob.blockBuilder(CRAFTING_MANAGER)
                        .ironPickaxeTags()
                        .blockState(DataGenHelper::createCraftingManager)
        );
    }
}