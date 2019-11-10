package mcjty.rftoolsstorage.modules.craftingmanager;

import mcjty.lib.blocks.BaseBlockItem;
import mcjty.lib.container.GenericContainer;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerBlock;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerContainer;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerTileEntity;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ObjectHolder;

public class CraftingManagerSetup {

    @ObjectHolder(RFToolsStorage.MODID + ":crafting_manager")
    public static CraftingManagerBlock CRAFTING_MANAGER;
    @ObjectHolder(RFToolsStorage.MODID + ":crafting_manager")
    public static TileEntityType<?> TYPE_CRAFTING_MANAGER;
    @ObjectHolder(RFToolsStorage.MODID + ":crafting_manager")
    public static ContainerType<CraftingManagerContainer> CONTAINER_CRAFTING_MANAGER;

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new CraftingManagerBlock());
    }

    public static void registerItems(RegistryEvent.Register<Item> event) {
        Item.Properties properties = new Item.Properties().group(RFToolsStorage.setup.getTab());
        event.getRegistry().register(new BaseBlockItem(CRAFTING_MANAGER, properties));
    }

    public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().register(TileEntityType.Builder.create(CraftingManagerTileEntity::new, CRAFTING_MANAGER).build(null).setRegistryName("crafting_manager"));
    }

    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().register(GenericContainer.createContainerType("crafting_manager"));
    }

    public static void initClient() {
        CRAFTING_MANAGER.initModel();
    }
}
