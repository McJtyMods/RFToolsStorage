package mcjty.rftoolsstorage.craftinggrid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.rftoolsstorage.modules.scanner.tools.SortingMode;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RFCraftingRecipe {
    private final List<ItemStack> inv = new ArrayList<>(9);
    {
        for (int i = 0; i < 9; i++) {
            inv.add(ItemStack.EMPTY);
        }
    }
    private ItemStack result = ItemStack.EMPTY;

    private boolean recipePresent = false;
    private Optional<RecipeHolder<CraftingRecipe>> recipe = Optional.empty();

    private boolean keepOne = false;

    public static final Codec<RFCraftingRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("inv").forGetter(r -> r.inv),
            ItemStack.OPTIONAL_CODEC.fieldOf("result").forGetter(RFCraftingRecipe::getResult),
            Codec.BOOL.fieldOf("keepOne").forGetter(RFCraftingRecipe::isKeepOne),
            CraftMode.CODEC.fieldOf("craftMode").forGetter(RFCraftingRecipe::getCraftMode)
    ).apply(instance, RFCraftingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RFCraftingRecipe> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_LIST_STREAM_CODEC, r -> r.inv,
            ItemStack.OPTIONAL_STREAM_CODEC, r -> r.result,
            ByteBufCodecs.BOOL, r -> r.keepOne,
            CraftMode.STREAM_CODEC, r -> r.craftMode,
            RFCraftingRecipe::new
    );

    public enum CraftMode implements StringRepresentable {
        EXT("Ext"),
        INT("Int"),
        EXTC("ExtC");

        public static final Codec<CraftMode> CODEC = StringRepresentable.fromEnum(CraftMode::values);
        public static final StreamCodec<FriendlyByteBuf, CraftMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(CraftMode.class);

        private final String description;

        CraftMode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }


        @Override
        public String getSerializedName() {
            return name();
        }
    }

    private CraftMode craftMode = CraftMode.EXT;

    public RFCraftingRecipe() {
    }

    public RFCraftingRecipe(List<ItemStack> inv, ItemStack result, boolean keepOne, CraftMode craftMode) {
        this.inv.addAll(inv);
        this.result = result;
        this.keepOne = keepOne;
        this.craftMode = craftMode;
    }

    public static Optional<RecipeHolder<CraftingRecipe>> findRecipe(Level world, CraftingInput inv) {
        return world.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, inv, world);
    }

    public void readFromNBT(CompoundTag tagCompound, HolderLookup.Provider provider) {
        ListTag nbtTagList = tagCompound.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < nbtTagList.size(); i++) {
            CompoundTag tag = nbtTagList.getCompound(i);
            inv.set(i, ItemStack.parseOptional(provider, tag));
        }
        CompoundTag resultCompound = tagCompound.getCompound("Result");
        result = ItemStack.parseOptional(provider, resultCompound);
        keepOne = tagCompound.getBoolean("Keep");
        craftMode = CraftMode.values()[tagCompound.getByte("Int")];
        recipePresent = false;
    }

    public void writeToNBT(CompoundTag tagCompound, HolderLookup.Provider provider) {
        ListTag nbtTagList = new ListTag();
        for (int i = 0 ; i < 9 ; i++) {
            ItemStack stack = inv.get(i);
            nbtTagList.add(stack.saveOptional(provider));
        }
        tagCompound.put("Result", result.saveOptional(provider));
        tagCompound.put("Items", nbtTagList);
        tagCompound.putBoolean("Keep", keepOne);
        tagCompound.putByte("Int", (byte) craftMode.ordinal());
    }

    public void setRecipe(ItemStack[] items, ItemStack result) {
        for (int i = 0 ; i < 9 ; i++) {
            inv.set(i, items[i]);
        }
        this.result = result;
        recipePresent = false;
    }

    public List<ItemStack> getInventory() {
        return inv;
    }

    public void setResult(ItemStack result) {
        this.result = result;
    }

    public ItemStack getResult() {
        return result;
    }

    public Optional<RecipeHolder<CraftingRecipe>> getCachedRecipe(Level world) {
        if (!recipePresent) {
            recipePresent = true;
            recipe = findRecipe(world, CraftingInput.ofPositioned(3, 3, inv).input());
        }
        return recipe;
    }

    public boolean isKeepOne() {
        return keepOne;
    }

    public void setKeepOne(boolean keepOne) {
        this.keepOne = keepOne;
    }

    public CraftMode getCraftMode() {
        return craftMode;
    }

    public void setCraftMode(CraftMode craftMode) {
        this.craftMode = craftMode;
    }
}
