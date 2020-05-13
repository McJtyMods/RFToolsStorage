package mcjty.rftoolsstorage.modules.craftingmanager;

import mcjty.lib.container.GenericContainer;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerBlock;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerContainer;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerTileEntity;
import mcjty.rftoolsstorage.setup.Registration;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;

import static mcjty.rftoolsstorage.setup.Registration.*;

public class CraftingManagerSetup {

    public static void register() {
        // Needed to force class loading
    }

    public static final RegistryObject<Block> CRAFTING_MANAGER = BLOCKS.register("crafting_manager", CraftingManagerBlock::new);
    public static final RegistryObject<Item> CRAFTING_MANAGER_ITEM = ITEMS.register("crafting_manager", () -> new BlockItem(CRAFTING_MANAGER.get(), Registration.createStandardProperties()));
    public static final RegistryObject<TileEntityType<CraftingManagerTileEntity>> TYPE_CRAFTING_MANAGER = TILES.register("crafting_manager", () -> TileEntityType.Builder.create(CraftingManagerTileEntity::new, CRAFTING_MANAGER.get()).build(null));
    public static final RegistryObject<ContainerType<CraftingManagerContainer>> CONTAINER_CRAFTING_MANAGER = CONTAINERS.register("crafting_manager", GenericContainer::createContainerType);
}