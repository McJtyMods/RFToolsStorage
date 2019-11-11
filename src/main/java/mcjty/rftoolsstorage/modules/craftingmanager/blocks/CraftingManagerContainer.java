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

    public static final int SLOTS_DEVICES = 0;
    public static final int SLOTS_DEVICE_0 = 4;
    public static final int SLOTS_DEVICE_1 = 4+8;
    public static final int SLOTS_DEVICE_2 = 4+16;
    public static final int SLOTS_DEVICE_3 = 4+24;

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(4 + 8*4) {
        @Override
        protected void setup() {
            slot(SlotDefinition.input(), CONTAINER_CONTAINER, SLOTS_DEVICES + 0, 22, 17);
            slot(SlotDefinition.input(), CONTAINER_CONTAINER, SLOTS_DEVICES + 1, 22, 17+22);
            slot(SlotDefinition.input(), CONTAINER_CONTAINER, SLOTS_DEVICES + 2, 22, 17+44);
            slot(SlotDefinition.input(), CONTAINER_CONTAINER, SLOTS_DEVICES + 3, 22, 17+66);
            box(SlotDefinition.specific(ItemStack.EMPTY), CONTAINER_CONTAINER, SLOTS_DEVICE_0, 85, 17, 8, 1);
            box(SlotDefinition.specific(ItemStack.EMPTY), CONTAINER_CONTAINER, SLOTS_DEVICE_1, 85, 17+22, 8, 1);
            box(SlotDefinition.specific(ItemStack.EMPTY), CONTAINER_CONTAINER, SLOTS_DEVICE_2, 85, 17+44, 8, 1);
            box(SlotDefinition.specific(ItemStack.EMPTY), CONTAINER_CONTAINER, SLOTS_DEVICE_3, 85, 17+66, 8, 1);
            playerSlots(85, 125);
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
