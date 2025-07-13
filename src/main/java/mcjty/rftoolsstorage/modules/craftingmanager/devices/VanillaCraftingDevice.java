package mcjty.rftoolsstorage.modules.craftingmanager.devices;

import mcjty.lib.crafting.BaseRecipe;
import mcjty.rftoolsbase.modules.crafting.items.CraftingCardItem;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.craftingmanager.system.ICraftingDevice;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VanillaCraftingDevice implements ICraftingDevice {

    public static final ResourceLocation DEVICE_VANILLA_CRAFTING = ResourceLocation.fromNamespaceAndPath(RFToolsStorage.MODID, "vanilla_crafting");

    List<ItemStack> inventory = new ArrayList<>();

    private ItemStack cardStack = ItemStack.EMPTY;
    private Recipe recipe;
    private int ticks = -1;

    @Override
    public ResourceLocation getID() {
        return DEVICE_VANILLA_CRAFTING;
    }

    @Override
    public void setupCraft(@Nonnull Level world, @Nonnull ItemStack cardStack) {
        this.cardStack = cardStack;
        recipe = CraftingCardItem.findRecipe(world, cardStack, getRecipeType());
    }

    @Override
    public List<Ingredient> getIngredients() {
        if (recipe != null) {
            return recipe.getIngredients();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void tick() {
        if (ticks > 0) {
            ticks--;
        }
    }

    @Override
    public boolean insertIngredients(Level world, List<ItemStack> items) {
        if (recipe == null) {
            return false;
        }
        if (getStatus() != Status.IDLE) {
            return false;
        }
        inventory.clear();
        for (ItemStack item : items) {
            inventory.add(item);
        }
        CraftingInput.Positioned inp = CraftingInput.ofPositioned(3, 3, inventory);
        if (!recipe.matches(inp.input(), world)) {
            inventory.clear();
            return false;
        }
        ticks = 10;
        return true;
    }

    @Override
    public ItemStack getCraftingItem(Level level) {
        CraftingInput.Positioned inp = CraftingInput.ofPositioned(3, 3, inventory);
        return BaseRecipe.assemble(recipe, inp.input(), level);
    }

    @Override
    public List<ItemStack> extractOutput(Level level) {
        if (getStatus() == Status.READY) {
            List<ItemStack> result = new ArrayList<>();
            ticks = -1;
            CraftingInput.Positioned inp = CraftingInput.ofPositioned(3, 3, inventory);
            ItemStack rc = BaseRecipe.assemble(recipe, inp.input(), level);
            if (!rc.isEmpty()) {
                result.add(rc);
            }
            for (Object item : recipe.getRemainingItems(inp.input())) {
                result.add((ItemStack) item);
            }

            for (int i = 0 ; i < inventory.size() ; i++) {
                // @todo should items left in the work inventory also be put back?
                inventory.set(i, ItemStack.EMPTY);
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
    public RecipeType<?> getRecipeType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public void read(HolderLookup.Provider provider, CompoundTag tag) {
        cardStack = ItemStack.parseOptional(provider, tag.getCompound("cardStack"));
        ticks = tag.getInt("ticks");
    }

    @Override
    public void write(HolderLookup.Provider provider, CompoundTag tag) {
        tag.putInt("ticks", ticks);
        CompoundTag compoundNBT = new CompoundTag();
        Tag stackTag = cardStack.save(provider, compoundNBT);
        tag.put("cardStack", stackTag);
    }
}
