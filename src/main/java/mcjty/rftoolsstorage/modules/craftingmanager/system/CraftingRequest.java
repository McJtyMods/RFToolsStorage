package mcjty.rftoolsstorage.modules.craftingmanager.system;

import net.minecraft.item.ItemStack;

public class CraftingRequest {

    private final ItemStack stack;
    private final int amount;

    // A chain of requests we already visited while crafting so that we can prevent loops
    private final CraftingRequest parentRequest;

    public CraftingRequest(ItemStack stack, int amount, CraftingRequest parentRequest) {
        this.stack = stack;
        this.amount = amount;
        this.parentRequest = parentRequest;
    }

    public ItemStack getStack() {
        return stack;
    }

    public int getAmount() {
        return amount;
    }

    public CraftingRequest getParentRequest() {
        return parentRequest;
    }
}
