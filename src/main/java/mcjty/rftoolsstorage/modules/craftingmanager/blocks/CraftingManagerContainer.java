package mcjty.rftoolsstorage.modules.craftingmanager.blocks;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.rftoolsbase.modules.crafting.items.CraftingCardItem;
import mcjty.rftoolsstorage.modules.craftingmanager.CraftingManagerModule;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import static mcjty.lib.container.ContainerFactory.CONTAINER_CONTAINER;
import static mcjty.lib.container.SlotDefinition.input;
import static mcjty.lib.container.SlotDefinition.specific;

public class CraftingManagerContainer extends GenericContainer {

    public static final int SLOTS_DEVICES = 0;
    public static final int SLOTS_DEVICE_0 = 4;
    public static final int SLOTS_DEVICE_1 = 4+8;
    public static final int SLOTS_DEVICE_2 = 4+16;
    public static final int SLOTS_DEVICE_3 = 4+24;

    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(4 + 8*4)
            .slot(input(), CONTAINER_CONTAINER, SLOTS_DEVICES + 0, 22, 17)
            .slot(input(), CONTAINER_CONTAINER, SLOTS_DEVICES + 1, 22, 17+22)
            .slot(input(), CONTAINER_CONTAINER, SLOTS_DEVICES + 2, 22, 17+44)
            .slot(input(), CONTAINER_CONTAINER, SLOTS_DEVICES + 3, 22, 17+66)
            .box(specific(s -> s.getItem() instanceof CraftingCardItem), CONTAINER_CONTAINER, SLOTS_DEVICE_0, 85, 17, 8, 1)
            .box(specific(s -> s.getItem() instanceof CraftingCardItem), CONTAINER_CONTAINER, SLOTS_DEVICE_1, 85, 17+22, 8, 1)
            .box(specific(s -> s.getItem() instanceof CraftingCardItem), CONTAINER_CONTAINER, SLOTS_DEVICE_2, 85, 17+44, 8, 1)
            .box(specific(s -> s.getItem() instanceof CraftingCardItem), CONTAINER_CONTAINER, SLOTS_DEVICE_3, 85, 17+66, 8, 1)
            .playerSlots(85, 125));

    public CraftingManagerContainer(int id, BlockPos pos, PlayerEntity player, CraftingManagerTileEntity tileEntity) {
        super(CraftingManagerModule.CONTAINER_CRAFTING_MANAGER.get(), id, CONTAINER_FACTORY.get(), pos, tileEntity);
    }

    @Override
    public void setupInventories(IItemHandler itemHandler, PlayerInventory inventory) {
        addInventory(CONTAINER_CONTAINER, itemHandler);        // The storage card itemhandler
        addInventory(ContainerFactory.CONTAINER_PLAYER, new InvWrapper(inventory));
        generateSlots();
    }
}
