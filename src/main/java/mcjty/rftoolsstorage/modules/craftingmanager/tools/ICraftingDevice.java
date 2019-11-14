package mcjty.rftoolsstorage.modules.craftingmanager.tools;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.world.World;

import java.util.List;

public interface ICraftingDevice {

    enum Status {
        IDLE,       // Doing nothing and no output ready
        BUSY,       // Busy working
        READY       // Doing nothing and output ready
    }

    /// Do one crafting tick
    void tick();

    /// Set the recipe to use for subsequent crafts
    void setRecipe(IRecipe recipe);

    /// Insert the ingredients. Returns false if this fails for some reason (in that case nothing will have been inserted)
    boolean insertIngredients(List<ItemStack> items, World world);

    /// Extract output
    List<ItemStack> extractOutput();

    /// Return the recipe type supported by this device
    IRecipeType<?> getRecipeType();

    /// Return the current status
    Status getStatus();
}
