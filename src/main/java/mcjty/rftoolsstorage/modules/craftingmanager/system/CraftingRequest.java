package mcjty.rftoolsstorage.modules.craftingmanager.system;

import net.minecraft.item.crafting.Ingredient;

public class CraftingRequest {

    private final Ingredient ingredient;
    private final int amount;

    // A chain of requests we already visited while crafting so that we can prevent loops
    private final CraftingRequest parentRequest;

    public CraftingRequest(Ingredient ingredient, int amount, CraftingRequest parentRequest) {
        this.ingredient = ingredient;
        this.amount = amount;
        this.parentRequest = parentRequest;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public int getAmount() {
        return amount;
    }

    public CraftingRequest getParentRequest() {
        return parentRequest;
    }
}
