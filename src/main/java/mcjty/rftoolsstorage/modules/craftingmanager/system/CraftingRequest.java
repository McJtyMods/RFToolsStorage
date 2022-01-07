package mcjty.rftoolsstorage.modules.craftingmanager.system;

import net.minecraft.world.item.crafting.Ingredient;

public record CraftingRequest(int id, Ingredient ingredient, int amount, int parentId) {
}
