package mcjty.rftoolsstorage.craftinggrid;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.SafeClientTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record PacketGridSync(
        BlockPos pos,
        ResourceKey<Level> type,
        List<Recipe> recipes) {

    public record Recipe(List<ItemStack> stacks) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Recipe> STREAM_CODEC = StreamCodec.composite(
                ItemStack.OPTIONAL_LIST_STREAM_CODEC, Recipe::stacks,
                Recipe::new
        );
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketGridSync> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), s -> Optional.ofNullable(s.pos),
            ResourceKey.streamCodec(Registries.DIMENSION).apply(ByteBufCodecs::optional), s -> Optional.ofNullable(s.type),
            Recipe.STREAM_CODEC.apply(ByteBufCodecs.list()), PacketGridSync::recipes,
            (pos, type, recipes) -> new PacketGridSync(pos.orElse(null), type.orElse(null), recipes)
    );

    public PacketGridSync(BlockPos pos, ResourceKey<Level> type, CraftingGrid grid) {
        this(pos, type, getRecipes(grid));
    }

    private static List<Recipe> getRecipes(CraftingGrid grid) {
        List<Recipe> recipes = new ArrayList<>();
        for (int i = 0 ; i < 6 ; i++) {
            RFCraftingRecipe recipe = grid.getRecipe(i);
            List<ItemStack> inventory = recipe.getInventory();
            List<ItemStack> stacks = new ArrayList<>();
            stacks.add(recipe.getResult());
            for (int j = 0 ; j < 9 ; j++) {
                stacks.add(inventory.get(j));
            }
            recipes.add(new Recipe(stacks));
        }
        return recipes;
    }

    public CraftingGridProvider handleMessage(Level world, Player player) {
        CraftingGridProvider provider = null;

        BlockEntity te;
        if (pos == null) {
            // We are working from a tablet. Find the tile entity through the open container
            GenericContainer container = getOpenContainer();
            if (container == null) {
                Logging.log("Container is missing!");
                return null;
            }
            te = container.getBe();
        } else {
            te = world.getBlockEntity(pos);
        }

        if (te instanceof CraftingGridProvider) {
            provider = ((CraftingGridProvider) te);
        }

        if (provider != null) {
            for (int i = 0; i < recipes.size(); i++) {
                List<ItemStack> stacks = recipes.get(i).stacks();
                provider.setRecipe(i, stacks.toArray(new ItemStack[0]));
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
