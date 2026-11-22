package mcjty.rftoolsstorage.craftinggrid;

import mcjty.lib.varia.InventoryTools;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import java.util.NoSuchElementException;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.apache.commons.lang3.tuple.Pair;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InventoriesItemSource implements IItemSource {

    private final List<Pair<ResourceHandler<ItemResource>, Integer>> inventories = new ArrayList<>();

    public InventoriesItemSource add(ResourceHandler<ItemResource> inventory, int offset) {
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
                    ResourceHandler<ItemResource> inventory = inventories.get(inventoryIndex).getLeft();
                    int offset = inventories.get(inventoryIndex).getRight();
                    if (slotIndex < offset) {
                        slotIndex = offset;
                    }
                    if (slotIndex < inventory.size()) {
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
                ResourceHandler<ItemResource> inventory = inventories.get(inventoryIndex).getLeft();
                ItemKey key = new ItemKey(inventory, slotIndex);
                Pair<IItemKey, ItemStack> result = Pair.of(key, ItemUtil.getStack(inventory, slotIndex));
                slotIndex++;
                return result;
            }
        };
    }

    @Override
    public ItemStack decrStackSize(IItemKey key, int amount) {
        ItemKey slot = (ItemKey) key;
        return InventoryTools.extractItem(slot.inventory(), slot.slot(), amount, false);
    }

    @Override
    public boolean insertStack(IItemKey key, ItemStack stack) {
        ItemKey slot = (ItemKey) key;
        if (!InventoryTools.insertItem(slot.inventory(), slot.slot(), stack, true).isEmpty()) {
            return false;
        }
        return InventoryTools.insertItem(slot.inventory(), slot.slot(), stack, false).isEmpty();
    }

    @Override
    public int insertStackAnySlot(IItemKey key, ItemStack stack) {
        ItemKey slot = (ItemKey) key;
        return InventoryTools.insertItem(slot.inventory(), stack, false).getCount();
    }

    private record ItemKey(ResourceHandler<ItemResource> inventory, int slot) implements IItemKey {
    }
}
