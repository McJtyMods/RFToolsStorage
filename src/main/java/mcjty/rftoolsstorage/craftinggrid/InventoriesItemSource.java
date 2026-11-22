package mcjty.rftoolsstorage.craftinggrid;

import java.util.NoSuchElementException;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
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
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
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
        ItemKey slot = (ItemKey) key;
        return slot.inventory().extractItem(slot.slot(), amount, false);
    }

    @Override
    public boolean insertStack(IItemKey key, ItemStack stack) {
        ItemKey slot = (ItemKey) key;
        if (!slot.inventory().insertItem(slot.slot(), stack, true).isEmpty()) {
            return false;
        }
        return slot.inventory().insertItem(slot.slot(), stack, false).isEmpty();
    }

    @Override
    public int insertStackAnySlot(IItemKey key, ItemStack stack) {
        ItemKey slot = (ItemKey) key;
        return ItemHandlerHelper.insertItem(slot.inventory(), stack, false).getCount();
    }

    private record ItemKey(IItemHandler inventory, int slot) implements IItemKey {
    }
}
