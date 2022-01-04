package mcjty.rftoolsstorage.modules.craftingmanager.system;

import net.minecraft.world.item.crafting.Ingredient;

public class CraftingRequest {

    private final Ingredient ingredient;
    private final int amount;
    private final int id;               // A unique id so that we can identify/load and persist requests

    // A chain of requests we already visited while crafting so that we can prevent loops. Set to -1 if there is no parent
    private final int parentId;

    public CraftingRequest(int id, Ingredient ingredient, int amount, int parentId) {
        this.id = id;
        this.ingredient = ingredient;
        this.amount = amount;
        this.parentId = parentId;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public int getAmount() {
        return amount;
    }

    public int getId() {
        return id;
    }

    public int getParentId() {
        return parentId;
    }
}
