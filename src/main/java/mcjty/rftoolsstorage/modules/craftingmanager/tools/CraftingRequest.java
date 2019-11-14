package mcjty.rftoolsstorage.modules.craftingmanager.tools;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class CraftingRequest {

    private final BlockPos requester;
    private final ItemStack stack;
    private final int amount;

    public CraftingRequest(ItemStack stack, int amount, BlockPos requester) {
        this.requester = requester;
        this.stack = stack;
        this.amount = amount;
    }

    public BlockPos getRequester() {
        return requester;
    }

    public ItemStack getStack() {
        return stack;
    }

    public int getAmount() {
        return amount;
    }
}
