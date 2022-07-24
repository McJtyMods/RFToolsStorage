package mcjty.rftoolsstorage.craftinggrid;

import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import mcjty.rftoolsstorage.setup.RFToolsStorageMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkDirection;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StorageCraftingTools {

    @Nonnull
    private static int[] tryRecipe(Player player, RFCraftingRecipe craftingRecipe, int n, IItemSource itemSource) {
        CraftingContainer workInventory = new CraftingContainer(new AbstractContainerMenu(null, -1) {
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

        int[] missingCount = new int[10];
        for (int i = 0; i < 10; i++) {
            missingCount[i] = 0;
            if (i < 9) {
                if (i < ingredients.size()) {
                    Ingredient ingredient = ingredients.get(i);
                    ItemStack[] stacks = ingredient.getItems();
                    if (stacks.length > 0) {
                        ItemStack stack = stacks[0];
                        if (!stack.isEmpty()) {
                            missingCount[i] = stack.getCount() * n;
                        }
                    }
                }
                workInventory.setItem(i, ItemStack.EMPTY);
            }
        }

        for (Pair<IItemKey, ItemStack> pair : itemSource.getItems()) {
            ItemStack input = pair.getValue();
            int size = input.getCount();
            if (!input.isEmpty()) {
                for (int i = 0; i < ingredients.size(); i++) {
                    if (missingCount[i] > 0) {
                        if (ingredients.get(i).test(input)) {
                            if (size > missingCount[i]) {
                                size -= missingCount[i];
                                missingCount[i] = 0;
                            } else {
                                missingCount[i] -= size;
                                size = 0;
                            }
                            workInventory.setItem(i, input.copy());
                        }
                    }
                }
            }
        }

        missingCount[9] = recipe.map(r -> r.matches(workInventory, player.getCommandSenderWorld()) ? 0 : 1).orElse(0);

        if (missingCount[9] == 0) {
            for (int i = 0; i < 9; i++) {
                if (missingCount[i] > 0) {
                    missingCount[9] = 1;
                    break;
                }
            }
        }

        return missingCount;
    }

    private static List<ItemStack> testAndConsumeCraftingItems(Player player, RFCraftingRecipe craftingRecipe,
                                                               IItemSource itemSource) {
        CraftingContainer workInventory = new CraftingContainer(new AbstractContainerMenu(null, -1) {
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
            ItemStack stack = r.assemble(workInventory);
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

            ItemStack recipeResult = r.getResultItem();
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
    public static int[] testCraftItems(Player player, int nn, RFCraftingRecipe craftingRecipe, IItemSource itemSource) {
        Optional<CraftingRecipe> recipe = craftingRecipe.getCachedRecipe(player.getCommandSenderWorld());
        if (!recipe.isPresent()) {
            // @todo give error?
            return new int[0];
        }

        final int[] n = {nn};
        return recipe.map(r -> {
            ItemStack recipeResult = r.getResultItem();
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

                int[] result = tryRecipe(player, craftingRecipe, n[0], itemSource);
                for (int i = 0; i < 10; i++) {
                    if (result[i] > 0) {
                        return result;
                    }
                }
                return result;
            } else {
                return new int[0];
            }
        }).orElse(new int[0]);
    }

    public static void craftFromGrid(Player player, int count, boolean test, BlockPos pos, ResourceKey<Level> type) {
//        player.addStat(StatList.CRAFTING_TABLE_INTERACTION);  // @todo 1.14
        int[] testResult = new int[0];
        BlockEntity te = LevelTools.getLevel(player.getCommandSenderWorld(), type).getBlockEntity(pos);
        if (te instanceof CraftingGridProvider) {
            testResult = ((CraftingGridProvider) te).craft(player, count, test);
        }
        if (testResult.length > 0) {
            RFToolsStorageMessages.INSTANCE.sendTo(new PacketCraftTestResultToClient(testResult), ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    public static void requestGridSync(Player player, BlockPos pos, ResourceKey<Level> type) {
        Level world = LevelTools.getLevel(player.getCommandSenderWorld(), type);
        CraftingGridProvider provider = null;
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof CraftingGridProvider && te instanceof GenericTileEntity) {
            provider = ((CraftingGridProvider) te);
        }
        boolean dummy = false;
        if (te instanceof StorageScannerTileEntity) {
            dummy = ((StorageScannerTileEntity) te).isDummy();
        }

        if (provider != null) {
            RFToolsStorageMessages.INSTANCE.sendTo(new PacketGridToClient(dummy ? null : pos, ((GenericTileEntity) te).getDimension(), provider.getCraftingGrid()), ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
    }
}
