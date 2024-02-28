package mcjty.rftoolsstorage.craftinggrid;

import mcjty.lib.crafting.BaseRecipe;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import mcjty.rftoolsstorage.setup.RFToolsStorageMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StorageCraftingTools {

    @Nonnull
    /// Try a recipe and return what's potentially missing
    private static List<Pair<ItemStack, Integer>> tryRecipe(Player player, RFCraftingRecipe craftingRecipe, int n, IItemSource itemSource) {
        CraftingContainer workInventory = new TransientCraftingContainer(new AbstractContainerMenu(null, -1) {
            @Override
            public boolean stillValid(@Nonnull Player var1) {
                return false;
            }

            @Override
            public ItemStack quickMoveStack(Player player, int slot) {
                return ItemStack.EMPTY;
            }
        }, 3, 3);

        Optional<CraftingRecipe> recipe = craftingRecipe.getCachedRecipe(player.getCommandSenderWorld());
        List<Ingredient> ingredients = recipe.map(Recipe::getIngredients).orElseGet(() -> NonNullList.withSize(9, Ingredient.EMPTY));

        List<Pair<ItemStack, Integer>> missing = new ArrayList<>(9);
        for (int i = 0 ; i < 9 ; i++) {
            missing.add(Pair.of(ItemStack.EMPTY, 0));
        }
        for (int i = 0; i < 9; i++) {
            if (i < ingredients.size()) {
                Ingredient ingredient = ingredients.get(i);
                ItemStack[] stacks = ingredient.getItems();
                if (stacks.length > 0) {
                    ItemStack stack = stacks[0];
                    if (!stack.isEmpty()) {
                        missing.set(i, Pair.of(stack, stack.getCount() * n));
                    }
                }
            }
            workInventory.setItem(i, ItemStack.EMPTY);
        }

        for (Pair<IItemKey, ItemStack> pair : itemSource.getItems()) {
            ItemStack input = pair.getValue();
            int size = input.getCount();
            if (!input.isEmpty()) {
                for (int i = 0; i < ingredients.size(); i++) {
                    if (missing.get(i).getRight() > 0) {
                        if (ingredients.get(i).test(input)) {
                            if (size > missing.get(i).getRight()) {
                                size -= missing.get(i).getRight();
                                missing.set(i, Pair.of(ItemStack.EMPTY, 0));
                            } else {
                                missing.set(i, Pair.of(missing.get(i).getLeft(), missing.get(i).getRight() - size));
                                size = 0;
                            }
                            workInventory.setItem(i, input.copy());
                        }
                    }
                }
            }
        }
        return missing.stream().filter(p -> p.getRight() > 0).collect(Collectors.toList());
    }

    private static List<ItemStack> testAndConsumeCraftingItems(Player player, RFCraftingRecipe craftingRecipe,
                                                               IItemSource itemSource) {
        CraftingContainer workInventory = new TransientCraftingContainer(new AbstractContainerMenu(null, -1) {
            @Override
            public boolean stillValid(@Nonnull Player var1) {
                return false;
            }

            @Override
            public ItemStack quickMoveStack(Player player, int slot) {
                return ItemStack.EMPTY;
            }
        }, 3, 3);

        List<Pair<IItemKey, ItemStack>> undo = new ArrayList<>();
        List<ItemStack> result = new ArrayList<>();

        Optional<CraftingRecipe> recipe = craftingRecipe.getCachedRecipe(player.getCommandSenderWorld());
        return recipe.map(r -> {
            int w = 3;
            int h = 3;
            if (r instanceof ShapedRecipe) {
                w = ((ShapedRecipe) r).getRecipeWidth();
                h = ((ShapedRecipe) r).getRecipeHeight();
            }
            List<Ingredient> ingredients = r.getIngredients();
            for (int x = 0 ; x < w ; x++) {
                for (int y = 0 ; y < h ; y++) {
                    int i = y * w + x;
                    int workIndex = y * 3 + x;
                    workInventory.setItem(workIndex, ItemStack.EMPTY);
                    if (i < ingredients.size()) {
                        Ingredient ingredient = ingredients.get(i);
                        ItemStack[] stacks = ingredient.getItems();
                        if (stacks.length > 0) {
                            ItemStack stack = stacks[0];
                            if (!stack.isEmpty()) {
                                int count = stack.getCount();
                                count = findMatchingItems(workInventory, undo, workIndex, ingredients.get(i), count, itemSource);

                                if (count > 0) {
                                    // Couldn't find all items.
                                    undo(player, itemSource, undo);
                                    return Collections.<ItemStack>emptyList();
                                }
                            }
                        }
                    }
                }
            }
            if (!r.matches(workInventory, player.getCommandSenderWorld())) {
                result.clear();
                undo(player, itemSource, undo);
                return result;
            }
            ItemStack stack = BaseRecipe.assemble(r, workInventory, player.level());
            if (!stack.isEmpty()) {
                result.add(stack);
                List<ItemStack> remaining = r.getRemainingItems(workInventory);
                for (ItemStack s : remaining) {
                    if (!s.isEmpty()) {
                        result.add(s);
                    }
                }
            } else {
                result.clear();
                undo(player, itemSource, undo);
            }
            return result;
        }).orElse(result);
    }

    private static int findMatchingItems(CraftingContainer workInventory,
                                         List<Pair<IItemKey, ItemStack>> undo, int i,
                                         @Nonnull Ingredient stack,
                                         int count, IItemSource itemSource) {
        for (Pair<IItemKey, ItemStack> pair : itemSource.getItems()) {
            ItemStack input = pair.getValue();
            if (!input.isEmpty()) {
                if (stack.test(input)) {
                    int ss = count;
                    if (input.getCount() - ss < 0) {
                        ss = input.getCount();
                    }
                    IItemKey key = pair.getKey();
                    ItemStack copy = input.copy();
                    ItemStack actuallyExtracted = itemSource.decrStackSize(key, ss);
                    if (actuallyExtracted.isEmpty()) {
                        // Failed
                    } else {
                        workInventory.setItem(i, copy);
                        count -= ss;
                        undo.add(Pair.of(key, actuallyExtracted));
                    }
                }
            }
            if (count == 0) {
                break;
            }
        }
        return count;
    }

    private static void undo(Player player, IItemSource itemSource, List<Pair<IItemKey, ItemStack>> undo) {
        for (Pair<IItemKey, ItemStack> pair : undo) {
            ItemStack stack = pair.getValue();
            if (!itemSource.insertStack(pair.getKey(), stack)) {
                // Insertion in original slot failed. Let's just try to insert it in any slot
                int amountLeft = itemSource.insertStackAnySlot(pair.getKey(), stack);
                if (amountLeft > 0) {
                    // We still have left-overs. Spawn them in the player inventory
                    ItemStack copy = stack.copy();
                    copy.setCount(amountLeft);
                    ItemHandlerHelper.giveItemToPlayer(player, copy);
                }
            }
        }
        player.containerMenu.broadcastChanges();
    }

    public static void craftItems(Player player, int nn, RFCraftingRecipe craftingRecipe, IItemSource itemSource) {
        Optional<CraftingRecipe> recipe = craftingRecipe.getCachedRecipe(player.getCommandSenderWorld());
        if (!recipe.isPresent()) {
            // @todo give error?
            return;
        }

        final int[] n = {nn};
        recipe.ifPresent(r -> {

            ItemStack recipeResult = BaseRecipe.getResultItem(r, player.level());
            if (!recipeResult.isEmpty() && recipeResult.getCount() > 0) {
                if (n[0] == -1) {
                    n[0] = recipeResult.getMaxStackSize();
                }

                int remainder = n[0] % recipeResult.getCount();
                n[0] /= recipeResult.getCount();
                if (remainder != 0) {
                    n[0]++;
                }
                if (n[0] * recipeResult.getCount() > recipeResult.getMaxStackSize()) {
                    n[0]--;
                }

                for (int i = 0; i < n[0]; i++) {
                    List<ItemStack> result = testAndConsumeCraftingItems(player, craftingRecipe, itemSource);
                    if (result.isEmpty()) {
                        return;
                    }
                    for (ItemStack stack : result) {
                        if (!player.getInventory().add(stack)) {
                            player.spawnAtLocation(stack, 1.05f);
                        }
                    }
                }
            }
        });
    }


    @Nonnull
    /**
     * Return a list of missing items together with how many are missing
     */
    public static List<Pair<ItemStack, Integer>> testCraftItems(Player player, int nn, RFCraftingRecipe craftingRecipe, IItemSource itemSource) {
        Optional<CraftingRecipe> recipe = craftingRecipe.getCachedRecipe(player.getCommandSenderWorld());
        if (!recipe.isPresent()) {
            // @todo give error?
            return Collections.emptyList();
        }

        final int[] n = {nn};
        return recipe.map(r -> {
            ItemStack recipeResult = BaseRecipe.getResultItem(r, player.level());
            if (!recipeResult.isEmpty() && recipeResult.getCount() > 0) {
                if (n[0] == -1) {
                    n[0] = recipeResult.getMaxStackSize();
                }

                int remainder = n[0] % recipeResult.getCount();
                n[0] /= recipeResult.getCount();
                if (remainder != 0) {
                    n[0]++;
                }
                if (n[0] * recipeResult.getCount() > recipeResult.getMaxStackSize()) {
                    n[0]--;
                }

                return tryRecipe(player, craftingRecipe, n[0], itemSource);
            } else {
                return Collections.<Pair<ItemStack, Integer>>emptyList();
            }
        }).orElse(Collections.emptyList());
    }

    public static void craftFromGrid(Player player, int count, boolean test, BlockPos pos, ResourceKey<Level> type) {
//        player.addStat(StatList.CRAFTING_TABLE_INTERACTION);  // @todo 1.14
        List<Pair<ItemStack, Integer>> testResult = Collections.emptyList();
        BlockEntity te = LevelTools.getLevel(player.getCommandSenderWorld(), type).getBlockEntity(pos);
        if (te instanceof CraftingGridProvider provider) {
            testResult = provider.craft(player, count, test);
        }
        if (!testResult.isEmpty()) {
            RFToolsStorageMessages.sendToPlayer(PacketCraftTestResultToClient.create(testResult), player);
        }
    }

    public static void requestGridSync(Player player, BlockPos pos, ResourceKey<Level> type) {
        Level world = LevelTools.getLevel(player.getCommandSenderWorld(), type);
        CraftingGridProvider provider = null;
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof CraftingGridProvider && te instanceof GenericTileEntity) {
            provider = ((CraftingGridProvider) te);
        }
        boolean dummy = te instanceof StorageScannerTileEntity scanner ? scanner.isDummy() : false;

        if (provider != null) {
            RFToolsStorageMessages.sendToPlayer(PacketGridToClient.create(dummy ? null : pos, ((GenericTileEntity) te).getDimension(), provider.getCraftingGrid()), player);
        }
    }
}
