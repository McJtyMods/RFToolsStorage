package mcjty.rftoolsstorage.craftinggrid;

import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CraftingGrid {

    private final CraftingGridInventory craftingGridInventory;
    private ItemStack[] lastPreviewInputs;
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
        recipes[index].setRecipe(Arrays.copyOfRange(stacks, 1, 10), stacks[0]);
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

    public void updateResult(Level level) {
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ItemStack[] ingredients = craftingGridInventory.getIngredients();
        if (lastPreviewInputs != null && ItemStack.listMatches(List.of(lastPreviewInputs), List.of(ingredients))) {
            return;
        }
        lastPreviewInputs = Arrays.stream(ingredients).map(ItemStack::copy).toArray(ItemStack[]::new);
        CraftingInput input = CraftingInput.ofPositioned(3, 3, List.of(ingredients)).input();
        ItemStack result = RFCraftingRecipe.findRecipe(level, input)
                .map(recipe -> recipe.value().assemble(input))
                .orElse(ItemStack.EMPTY);
        craftingGridInventory.setStackInSlot(CraftingGridInventory.SLOT_GHOSTOUTPUT, result);
    }

    public CompoundTag writeToNBT(HolderLookup.Provider provider) {
        CompoundTag tagCompound = new CompoundTag();
        ListTag bufferTagList = new ListTag();
        for (int i = 0 ; i < craftingGridInventory.getSlots() ; i++) {
            ItemStack stack = craftingGridInventory.getStackInSlot(i);
            Tag tag = CraftingData.writeStack(provider, stack);
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
        ListTag bufferTagList = tagCompound.getListOrEmpty("grid");
        for (int i = 0 ; i < craftingGridInventory.getSlots() ; i++) {
            CompoundTag tag = bufferTagList.getCompoundOrEmpty(i);
            craftingGridInventory.setStackInSlot(i, CraftingData.readStack(provider, tag));
        }

        ListTag recipeTagList = tagCompound.getListOrEmpty("recipes");
        for (int i = 0 ; i < Math.min(recipes.length, recipeTagList.size()) ; i++) {
            recipes[i] = new RFCraftingRecipe();
            CompoundTag tag = recipeTagList.getCompoundOrEmpty(i);
            recipes[i].readFromNBT(tag, provider);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        CraftingGrid that = (CraftingGrid) o;
        return Objects.equals(craftingGridInventory, that.craftingGridInventory) && Arrays.equals(recipes, that.recipes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(craftingGridInventory);
        result = 31 * result + Arrays.hashCode(recipes);
        return result;
    }
}
