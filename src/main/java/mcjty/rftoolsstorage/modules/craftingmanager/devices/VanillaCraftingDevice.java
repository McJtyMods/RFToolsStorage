package mcjty.rftoolsstorage.modules.craftingmanager.devices;

import mcjty.rftoolsstorage.modules.craftingmanager.ICraftingDevice;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VanillaCraftingDevice implements ICraftingDevice {

    private CraftingInventory inventory = new CraftingInventory(new Container(null, -1) {
        @Override
        public boolean canInteractWith(PlayerEntity playerIn) {
            return false;
        }
    }, 3, 3);

    private IRecipe recipe;
    private int ticks = -1;

    @Override
    public void setRecipe(IRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void tick() {
        if (ticks > 0) {
            ticks--;
        }
    }

    @Override
    public boolean insertIngredients(List<ItemStack> items, World world) {
        if (recipe == null) {
            return false;
        }
        if (getStatus() != Status.IDLE) {
            return false;
        }
        for (int i = 0 ; i < items.size() ; i++) {
            inventory.setInventorySlotContents(i, items.get(i).copy());
        }
        if (!recipe.matches(inventory, world)) {
            for (int i = 0 ; i < inventory.getSizeInventory() ; i++) {
                inventory.setInventorySlotContents(i, ItemStack.EMPTY);
            }
            return false;
        }
        ticks = 10;
        return true;
    }

    @Override
    public List<ItemStack> extractOutput() {
        if (getStatus() == Status.READY) {
            List<ItemStack> result = new ArrayList<>();
            ticks = -1;
            ItemStack rc = recipe.getCraftingResult(inventory);
            if (!rc.isEmpty()) {
                result.add(rc);
            }
            for (Object item : recipe.getRemainingItems(inventory)) {
                result.add((ItemStack) item);
            }

            for (int i = 0 ; i < inventory.getSizeInventory() ; i++) {
                // @todo should items left in the work inventory also be put back?
                inventory.setInventorySlotContents(i, ItemStack.EMPTY);
            }
            return result;
        }
        return Collections.emptyList();
    }

    @Override
    public Status getStatus() {
        if (ticks == -1) {
            return Status.IDLE;
        } else if (ticks == 0) {
            return Status.READY;
        }
        return Status.BUSY;
    }

    @Override
    public IRecipeType<?> getRecipeType() {
        return IRecipeType.CRAFTING;
    }
}
