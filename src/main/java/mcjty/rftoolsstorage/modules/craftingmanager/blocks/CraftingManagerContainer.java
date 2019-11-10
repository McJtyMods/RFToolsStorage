package mcjty.rftoolsstorage.modules.craftingmanager.blocks;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.SlotDefinition;
import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerSetup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class CraftingManagerContainer extends GenericContainer {

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(8*4) {
        @Override
        protected void setup() {
            box(SlotDefinition.specific(ItemStack.EMPTY), CONTAINER_CONTAINER, 0, 5, 5, 4, 8);  // @todo not right!
            playerSlots(91, 157);
        }

    };

    public CraftingManagerContainer(int id, BlockPos pos, PlayerEntity player, CraftingManagerTileEntity tileEntity) {
        super(CraftingManagerSetup.CONTAINER_CRAFTING_MANAGER, id, CONTAINER_FACTORY, pos, tileEntity);
    }

    @Override
    public void setupInventories(IItemHandler itemHandler, PlayerInventory inventory) {
        addInventory(ContainerFactory.CONTAINER_CONTAINER, itemHandler);        // The storage card itemhandler
        addInventory(ContainerFactory.CONTAINER_PLAYER, new InvWrapper(inventory));
        generateSlots();
    }
}
