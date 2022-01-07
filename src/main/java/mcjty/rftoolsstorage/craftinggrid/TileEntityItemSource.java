package mcjty.rftoolsstorage.craftinggrid;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TileEntityItemSource implements IItemSource {

    private final List<Pair<IItemHandler, Integer>> inventories = new ArrayList<>();

    public TileEntityItemSource add(BlockEntity te, int offset) {
        te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h ->
                inventories.add(Pair.of(h, offset)));
        return this;
    }

    public TileEntityItemSource add(IItemHandler handler, int offset) {
        inventories.add(Pair.of(handler, offset));
        return this;
    }

    private static ItemStack getStackInSlot(Object inv, int slot) {
        if (inv instanceof IItemHandler) {
            return ((IItemHandler) inv).getStackInSlot(slot);
        } else if (inv instanceof Container) {
            return ((Container) inv).getItem(slot);
        }
        return ItemStack.EMPTY;
    }

    private static boolean insertStackInSlot(Object inv, int slot, ItemStack stack) {
        if (inv instanceof IItemHandler handler) {
            if (!handler.insertItem(slot, stack, true).isEmpty()) {
                return false;
            }
            return handler.insertItem(slot, stack, false).isEmpty();
        } else if (inv instanceof Container inventory) {
            ItemStack oldStack = inventory.getItem(slot);
            if (!oldStack.isEmpty()) {
                if ((stack.getCount() + oldStack.getCount()) > stack.getMaxStackSize()) {
                    return false;
                }
                stack.grow(oldStack.getCount());
            }
            inventory.setItem(slot, stack);
            return true;
        }
        return false;
    }

    private static int insertStackInAnySlot(Object inv, ItemStack stack) {
        if (inv instanceof IItemHandler handler) {
            ItemStack leftOver = ItemHandlerHelper.insertItem(handler, stack, false);
            return leftOver.getCount();
        } else if (inv instanceof Container inventory) {
            // @todo 1.14
//            return InventoryHelper.mergeItemStack(inventory, true, stack, 0, inventory.getSizeInventory(), null);
            return 0;
        }
        return stack.getCount();
    }

    private static int getSizeInventory(Object inv) {
        if (inv instanceof IItemHandler) {
            return ((IItemHandler) inv).getSlots();
        } else if (inv instanceof Container) {
            return ((Container) inv).getContainerSize();
        }
        return 0;
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
                    Object inventory = inventories.get(inventoryIndex).getLeft();
                    if (slotIndex < getSizeInventory(inventory)) {
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
                IItemHandler te = inventories.get(inventoryIndex).getLeft();

                ItemKey key = new ItemKey(te, slotIndex);
                Pair<IItemKey, ItemStack> result = Pair.of(key, getStackInSlot(te, slotIndex));
                slotIndex++;
                return result;
            }
        };
    }

    @Override
    public ItemStack decrStackSize(IItemKey key, int amount) {
        ItemKey realKey = (ItemKey) key;
        IItemHandler handler = realKey.inventory();
        if (handler != null) {
            return handler.extractItem(realKey.slot(), amount, false);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean insertStack(IItemKey key, ItemStack stack) {
        ItemKey realKey = (ItemKey) key;
        return insertStackInSlot(realKey.inventory(), realKey.slot(), stack);
    }

    @Override
    public int insertStackAnySlot(IItemKey key, ItemStack stack) {
        ItemKey realKey = (ItemKey) key;
        return insertStackInAnySlot(realKey.inventory(), stack);
    }

    private record ItemKey(IItemHandler inventory, int slot) implements IItemKey {
    }
}
