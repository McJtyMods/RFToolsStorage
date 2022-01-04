package mcjty.rftoolsstorage.modules.craftingmanager.devices;

import mcjty.rftoolsbase.modules.crafting.items.CraftingCardItem;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.craftingmanager.system.ICraftingDevice;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mcjty.rftoolsstorage.modules.craftingmanager.system.ICraftingDevice.Status;

public class VanillaCraftingDevice implements ICraftingDevice {

    public static final ResourceLocation DEVICE_VANILLA_CRAFTING = new ResourceLocation(RFToolsStorage.MODID, "vanilla_crafting");

    private CraftingContainer inventory = new CraftingContainer(new AbstractContainerMenu(null, -1) {
        @Override
        public boolean stillValid(@Nonnull Player playerIn) {
            return false;
        }
    }, 3, 3);

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
        for (int i = 0 ; i < items.size() ; i++) {
            inventory.setItem(i, items.get(i).copy());
        }
        if (!recipe.matches(inventory, world)) {
            for (int i = 0 ; i < inventory.getContainerSize() ; i++) {
                inventory.setItem(i, ItemStack.EMPTY);
            }
            return false;
        }
        ticks = 10;
        return true;
    }

    @Override
    public ItemStack getCraftingItem() {
        return recipe.assemble(inventory);
    }

    @Override
    public List<ItemStack> extractOutput() {
        if (getStatus() == Status.READY) {
            List<ItemStack> result = new ArrayList<>();
            ticks = -1;
            ItemStack rc = recipe.assemble(inventory);
            if (!rc.isEmpty()) {
                result.add(rc);
            }
            for (Object item : recipe.getRemainingItems(inventory)) {
                result.add((ItemStack) item);
            }

            for (int i = 0 ; i < inventory.getContainerSize() ; i++) {
                // @todo should items left in the work inventory also be put back?
                inventory.setItem(i, ItemStack.EMPTY);
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
    public void read(CompoundTag tag) {
        cardStack = ItemStack.of(tag.getCompound("cardStack"));
        ticks = tag.getInt("ticks");
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putInt("ticks", ticks);
        CompoundTag compoundNBT = new CompoundTag();
        cardStack.save(compoundNBT);
        tag.put("cardStack", compoundNBT);
    }
}
