package mcjty.rftoolsstorage.modules.craftingmanager.devices;

import mcjty.rftoolsbase.modules.crafting.items.CraftingCardItem;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.craftingmanager.system.ICraftingDevice;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mcjty.rftoolsstorage.modules.craftingmanager.system.ICraftingDevice.Status;

public class VanillaCraftingDevice implements ICraftingDevice {

    public static final ResourceLocation DEVICE_VANILLA_CRAFTING = new ResourceLocation(RFToolsStorage.MODID, "vanilla_crafting");

    private CraftingInventory inventory = new CraftingInventory(new Container(null, -1) {
        @Override
        public boolean stillValid(@Nonnull PlayerEntity playerIn) {
            return false;
        }
    }, 3, 3);

    private ItemStack cardStack = ItemStack.EMPTY;
    private IRecipe recipe;
    private int ticks = -1;

    @Override
    public ResourceLocation getID() {
        return DEVICE_VANILLA_CRAFTING;
    }

    @Override
    public void setupCraft(@Nonnull World world, @Nonnull ItemStack cardStack) {
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
    public boolean insertIngredients(World world, List<ItemStack> items) {
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
    public IRecipeType<?> getRecipeType() {
        return IRecipeType.CRAFTING;
    }

    @Override
    public void read(CompoundNBT tag) {
        cardStack = ItemStack.of(tag.getCompound("cardStack"));
        ticks = tag.getInt("ticks");
    }

    @Override
    public void write(CompoundNBT tag) {
        tag.putInt("ticks", ticks);
        CompoundNBT compoundNBT = new CompoundNBT();
        cardStack.save(compoundNBT);
        tag.put("cardStack", compoundNBT);
    }
}
