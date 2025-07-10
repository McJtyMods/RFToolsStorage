package mcjty.rftoolsstorage.craftinggrid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CraftingGrid {

    private final CraftingGridInventory craftingGridInventory;
    private final RFCraftingRecipe[] recipes = new RFCraftingRecipe[6];

    public static final Codec<CraftingGrid> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CraftingGridInventory.CODEC.fieldOf("inventory").forGetter(CraftingGrid::getCraftingGridInventory),
            RFCraftingRecipe.CODEC.listOf().fieldOf("recipes").forGetter(grid -> List.of(grid.recipes))
    ).apply(instance, CraftingGrid::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingGrid> STREAM_CODEC = StreamCodec.composite(
            CraftingGridInventory.STREAM_CODEC, CraftingGrid::getCraftingGridInventory,
            RFCraftingRecipe.STREAM_CODEC.apply(ByteBufCodecs.list()), s -> List.of(s.recipes),
            CraftingGrid::new);

    public CraftingGrid() {
        this.craftingGridInventory = new CraftingGridInventory();
        for (int i = 0 ; i < 6 ; i++) {
            recipes[i] = new RFCraftingRecipe();
        }
    }

    public CraftingGrid(CraftingGridInventory craftingGridInventory, List<RFCraftingRecipe> recipes) {
        this.craftingGridInventory = craftingGridInventory;
        for (int i = 0 ; i < 6 ; i++) {
            if (i < recipes.size()) {
                this.recipes[i] = recipes.get(i);
            } else {
                this.recipes[i] = new RFCraftingRecipe();
            }
        }
    }

    public void set(CraftingGrid grid) {
        this.craftingGridInventory.set(grid.craftingGridInventory);
        for (int i = 0 ; i < 6 ; i++) {
            this.recipes[i] = grid.recipes[i];
        }
    }

    public CraftingGridInventory getCraftingGridInventory() {
        return craftingGridInventory;
    }

    public RFCraftingRecipe getRecipe(int index) {
        return recipes[index];
    }

    public RFCraftingRecipe getActiveRecipe() {
        RFCraftingRecipe recipe = new RFCraftingRecipe();
        recipe.setRecipe(craftingGridInventory.getIngredients(), craftingGridInventory.getResult());
        return recipe;
    }

    public void setRecipe(int index, ItemStack[] stacks) {
        RFCraftingRecipe recipe = recipes[index];
        recipe.setResult(stacks[0]);
        for (int i = 0 ; i < 9 ; i++) {
            recipe.getInventory().set(i, stacks[i+1]);
        }
    }

    public void storeRecipe(int index) {
        RFCraftingRecipe recipe = getRecipe(index);
        recipe.setRecipe(craftingGridInventory.getIngredients(), craftingGridInventory.getResult());
    }

    public void selectRecipe(int index) {
        RFCraftingRecipe recipe = getRecipe(index);
        craftingGridInventory.setStackInSlot(CraftingGridInventory.SLOT_GHOSTOUTPUT, recipe.getResult());
        for (int i = 0 ; i < 9 ; i++) {
            craftingGridInventory.setStackInSlot(i+CraftingGridInventory.SLOT_GHOSTINPUT, recipe.getInventory().get(i));
        }
    }

    public CompoundTag writeToNBT(HolderLookup.Provider provider) {
        CompoundTag tagCompound = new CompoundTag();
        ListTag bufferTagList = new ListTag();
        for (int i = 0 ; i < craftingGridInventory.getSlots() ; i++) {
            ItemStack stack = craftingGridInventory.getStackInSlot(i);
            Tag tag = stack.saveOptional(provider);
            bufferTagList.add(tag);
        }
        tagCompound.put("grid", bufferTagList);

        ListTag recipeTagList = new ListTag();
        for (RFCraftingRecipe recipe : recipes) {
            CompoundTag tag = new CompoundTag();
            recipe.writeToNBT(tag, provider);
            recipeTagList.add(tag);
        }
        tagCompound.put("recipes", recipeTagList);

        return tagCompound;
    }

    public void readFromNBT(CompoundTag tagCompound, HolderLookup.Provider provider) {
        if (tagCompound == null) {
            return;
        }
        ListTag bufferTagList = tagCompound.getList("grid", Tag.TAG_COMPOUND);
        for (int i = 0 ; i < craftingGridInventory.getSlots() ; i++) {
            CompoundTag tag = bufferTagList.getCompound(i);
            craftingGridInventory.setStackInSlot(i, ItemStack.parseOptional(provider, tag));
        }

        ListTag recipeTagList = tagCompound.getList("recipes", Tag.TAG_COMPOUND);
        for (int i = 0 ; i < recipeTagList.size() ; i++) {
            recipes[i] = new RFCraftingRecipe();
            CompoundTag tag = recipeTagList.getCompound(i);
            recipes[i].readFromNBT(tag, provider);
        }
    }
}
