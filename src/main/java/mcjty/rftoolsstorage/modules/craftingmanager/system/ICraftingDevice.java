package mcjty.rftoolsstorage.modules.craftingmanager.system;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

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
    void setupCraft(@Nonnull World world, @Nonnull ItemStack cardStack);

    List<Ingredient> getIngredients();

    /**
     * Return the main item that this device will craft
     */
    ItemStack getCraftingItem();

    /**
     * Insert the ingredients. Returns false if this fails for some reason (in that case nothing will have been inserted).
     * The device will be put in BUSY mode.
     */
    boolean insertIngredients(World world, List<ItemStack> items);

    /// Extract output and put the device back in IDLE mode
    List<ItemStack> extractOutput();

    /// Return the recipe type supported by this device
    IRecipeType<?> getRecipeType();

    /// Return the current status
    Status getStatus();

    void read(CompoundNBT tag);
    void write(CompoundNBT tag);
}
