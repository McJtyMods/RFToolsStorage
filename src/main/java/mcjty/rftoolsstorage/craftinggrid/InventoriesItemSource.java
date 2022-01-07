package mcjty.rftoolsstorage.craftinggrid;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InventoriesItemSource implements IItemSource {

    private final List<Pair<IItemHandler, Integer>> inventories = new ArrayList<>();

    public InventoriesItemSource add(IItemHandler inventory, int offset) {
        inventories.add(Pair.of(inventory, offset));
        return this;
    }

    @Override
    public Iterable<Pair<IItemKey, ItemStack>> getItems() {
        return () -> new Iterator<>() {
            private int inventoryIndex = 0;
            private int slotIndex = 0;

            private boolean firstValidItem() {
                while (true) {
                    if (inventoryIndex >= inventories.size()) {
                        return false;
                    }
                    IItemHandler inventory = inventories.get(inventoryIndex).getLeft();
                    int offset = inventories.get(inventoryIndex).getRight();
                    if (slotIndex < offset) {
                        slotIndex = offset;
                    }
                    if (slotIndex < inventory.getSlots()) {
                        return true;
                    } else {
                        slotIndex = 0;
                        inventoryIndex++;
                    }
                }
            }

            @Override
            public boolean hasNext() {
                return firstValidItem();
            }

            @Override
            public Pair<IItemKey, ItemStack> next() {
                IItemHandler inventory = inventories.get(inventoryIndex).getLeft();
                ItemKey key = new ItemKey(inventory, slotIndex);
                Pair<IItemKey, ItemStack> result = Pair.of(key, inventory.getStackInSlot(slotIndex));
                slotIndex++;
                return result;
            }
        };
    }

    @Override
    public ItemStack decrStackSize(IItemKey key, int amount) {
        ItemKey realKey = (ItemKey) key;
        ItemStack stack = realKey.inventory().getStackInSlot(realKey.slot());
        ItemStack result = stack.split(amount);
        if (stack.isEmpty()) {
            // @todo 1.14 is this really required?
//            realKey.getInventory().setInventorySlotContents(realKey.getSlot(), ItemStack.EMPTY);
        }
        return result;
    }

    @Override
    public boolean insertStack(IItemKey key, ItemStack stack) {
        ItemKey realKey = (ItemKey) key;
        IItemHandler inventory = realKey.inventory();
        ItemStack origStack = inventory.extractItem(realKey.slot(), 64, false);
        if (!origStack.isEmpty()) {
            if (ItemHandlerHelper.canItemStacksStack(origStack, stack)) {
                if ((stack.getCount() + origStack.getCount()) > stack.getMaxStackSize()) {
                    return false;
                }
                stack.grow(origStack.getCount());
            } else {
                return false;
            }
        }
        inventory.insertItem(realKey.slot(), stack, false);
        return true;
    }

    @Override
    public int insertStackAnySlot(IItemKey key, ItemStack stack) {
        ItemKey realKey = (ItemKey) key;
        IItemHandler inventory = realKey.inventory();
        // @todo 1.14
//        return InventoryHelper.mergeItemStack(inventory, true, stack, 0, inventory.getSizeInventory(), null);
        return 0;
    }

    private record ItemKey(IItemHandler inventory, int slot) implements IItemKey {
    }
}
