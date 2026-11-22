package mcjty.rftoolsstorage.craftinggrid;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CraftingGridInventory implements ResourceHandler<ItemResource>, IndexModifier<ItemResource> {

    public static final int SLOT_GHOSTOUTPUT = 0;
    public static final int SLOT_GHOSTINPUT = 1;

    public static final int GRID_WIDTH = 66;
    public static final int GRID_HEIGHT = 208;
    public static final int GRID_XOFFSET = -GRID_WIDTH - 2 + 7;
    public static final int GRID_YOFFSET = 127;

    private final List<ItemStack> stacks = new ArrayList<>();

    public static final Codec<CraftingGridInventory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("stacks").forGetter(s -> s.stacks)
    ).apply(instance, CraftingGridInventory::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingGridInventory> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_LIST_STREAM_CODEC, s -> s.stacks,
            CraftingGridInventory::new
    );

    public ItemStack getResult() {
        return getStackInSlot(SLOT_GHOSTOUTPUT);
    }

    public CraftingGridInventory() {
    }

    public CraftingGridInventory(List<ItemStack> stacks) {
        this.stacks.clear();
        this.stacks.addAll(stacks);
        while (this.stacks.size() < 10) {
            this.stacks.add(ItemStack.EMPTY);
        }
    }

    public void set(CraftingGridInventory inventory) {
        this.stacks.clear();
        this.stacks.addAll(inventory.stacks);
        while (this.stacks.size() < 10) {
            this.stacks.add(ItemStack.EMPTY);
        }
    }

    public ItemStack[] getIngredients() {
        ItemStack[] ing = new ItemStack[9];
        for (int i = 0; i < ing.length; i++) {
            ing[i] = getStackInSlot(i + SLOT_GHOSTINPUT);
        }
        return ing;
    }

    @Override
    public int size() {
        return getSlots();
    }

    @Override
    public ItemResource getResource(int slot) {
        return ItemResource.of(getStackInSlot(slot));
    }

    @Override
    public long getAmountAsLong(int slot) {
        return getStackInSlot(slot).getCount();
    }

    @Override
    public long getCapacityAsLong(int slot, ItemResource resource) {
        return 0;
    }

    @Override
    public boolean isValid(int slot, ItemResource resource) {
        return false;
    }

    @Override
    public int insert(int slot, ItemResource resource, int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(int slot, ItemResource resource, int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public void set(int slot, ItemResource resource, int amount) {
        setStackInSlot(slot, resource.toStack(amount));
    }

    public int getSlots() {
        return 10;
    }

    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        while (this.stacks.size() < 10) {
            this.stacks.add(ItemStack.EMPTY);
        }
        stacks.set(slot, stack);
    }

    @Nonnull
    public ItemStack getStackInSlot(int index) {
        return index < stacks.size() ? stacks.get(index) : ItemStack.EMPTY;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        CraftingGridInventory that = (CraftingGridInventory) o;
        return stacks.equals(that.stacks);
    }

    @Override
    public int hashCode() {
        return stacks.hashCode();
    }
}
