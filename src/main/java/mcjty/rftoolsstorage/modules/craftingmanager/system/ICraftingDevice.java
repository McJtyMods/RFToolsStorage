package mcjty.rftoolsstorage.modules.craftingmanager.system;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;

public interface ICraftingDevice {

    enum Status {
        IDLE,       // Doing nothing and no output ready
        BUSY,       // Busy working
        READY       // Doing nothing and output ready
    }

    /// Get the ID of this crafting device
    ResourceLocation getID();

    /// Do one crafting tick. If the craft has finished the device will be put in READY mode
    void tick();

    /// Setup this device for a crafting operation
    void setupCraft(@Nonnull Level world, @Nonnull ItemStack cardStack);

    List<Ingredient> getIngredients();

    /**
     * Return the main item that this device will craft
     */
    ItemStack getCraftingItem(Level level);

    /**
     * Insert the ingredients. Returns false if this fails for some reason (in that case nothing will have been inserted).
     * The device will be put in BUSY mode.
     */
    boolean insertIngredients(Level world, List<ItemStack> items);

    /// Extract output and put the device back in IDLE mode
    List<ItemStack> extractOutput(Level level);

    /// Return the recipe type supported by this device
    RecipeType<?> getRecipeType();

    /// Return the current status
    Status getStatus();

    void read(CompoundTag tag);
    void write(CompoundTag tag);
}
