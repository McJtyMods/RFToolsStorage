package mcjty.rftoolsstorage.modules.craftingmanager;

import mcjty.lib.container.GenericContainer;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerBlock;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerContainer;
import mcjty.rftoolsstorage.modules.craftingmanager.blocks.CraftingManagerTileEntity;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static mcjty.rftoolsstorage.RFToolsStorage.MODID;

public class CraftingManagerSetup {

    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<TileEntityType<?>> TILES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, MODID);
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = new DeferredRegister<>(ForgeRegistries.CONTAINERS, MODID);

    public static void register() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<Block> CRAFTING_MANAGER = BLOCKS.register("crafting_manager", CraftingManagerBlock::new);
    public static final RegistryObject<Item> CRAFTING_MANAGER_ITEM = ITEMS.register("crafting_manager", () -> new BlockItem(CRAFTING_MANAGER.get(), RFToolsStorage.createStandardProperties()));
    public static final RegistryObject<TileEntityType<?>> TYPE_CRAFTING_MANAGER = TILES.register("crafting_manager", () -> TileEntityType.Builder.create(CraftingManagerTileEntity::new, CRAFTING_MANAGER.get()).build(null));
    public static final RegistryObject<ContainerType<CraftingManagerContainer>> CONTAINER_CRAFTING_MANAGER = CONTAINERS.register("crafting_manager", GenericContainer::createContainerType);
}