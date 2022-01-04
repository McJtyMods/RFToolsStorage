package mcjty.rftoolsstorage.craftinggrid;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.SafeClientTools;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class PacketGridSync {

    protected BlockPos pos;
    protected ResourceKey<Level> type;
    private List<ItemStack[]> recipes;

    public void convertFromBytes(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            pos = buf.readBlockPos();
        } else {
            pos = null;
        }
        type = LevelTools.getId(buf.readResourceLocation());
        int s = buf.readInt();
        recipes = new ArrayList<>(s);
        for (int i = 0 ; i < s ; i++) {
            int ss = buf.readInt();
            ItemStack[] stacks = new ItemStack[ss];
            for (int j = 0 ; j < ss ; j++) {
                stacks[j] = buf.readItem();
            }
            recipes.add(stacks);
        }
    }

    public void convertToBytes(FriendlyByteBuf buf) {
        if (pos != null) {
            buf.writeBoolean(true);
            buf.writeBlockPos(pos);
        } else {
            buf.writeBoolean(false);
        }
        buf.writeResourceLocation(type.location());
        buf.writeInt(recipes.size());
        for (ItemStack[] recipe : recipes) {
            buf.writeInt(recipe.length);
            for (ItemStack stack : recipe) {
                buf.writeItem(stack);
            }
        }
    }

    protected void init(BlockPos pos, ResourceKey<Level> type, CraftingGrid grid) {
        this.pos = pos;
        this.type = type;
        recipes = new ArrayList<>();
        for (int i = 0 ; i < 6 ; i++) {
            CraftingRecipe recipe = grid.getRecipe(i);
            CraftingContainer inventory = recipe.getInventory();
            ItemStack[] stacks = new ItemStack[10];
            stacks[0] = recipe.getResult();
            for (int j = 0 ; j < 9 ; j++) {
                stacks[j+1] = inventory.getItem(j);
            }
            recipes.add(stacks);
        }
    }

    protected CraftingGridProvider handleMessage(Level world, Player player) {
        CraftingGridProvider provider = null;

        BlockEntity te;
        if (pos == null) {
            // We are working from a tablet. Find the tile entity through the open container
            GenericContainer container = getOpenContainer();
            if (container == null) {
                Logging.log("Container is missing!");
                return null;
            }
            te = container.getTe();
        } else {
            te = world.getBlockEntity(pos);
        }

        if (te instanceof CraftingGridProvider) {
            provider = ((CraftingGridProvider) te);
        }

        if (provider != null) {
            for (int i = 0; i < recipes.size(); i++) {
                provider.setRecipe(i, recipes.get(i));
            }
        }
        return provider;
    }

    private static GenericContainer getOpenContainer() {
        AbstractContainerMenu container = SafeClientTools.getClientPlayer().containerMenu;
        if (container instanceof GenericContainer) {
            return (GenericContainer) container;
        } else {
            return null;
        }
    }

}
