package mcjty.rftoolsstorage.craftinggrid;

import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.WorldTools;
import mcjty.rftoolsstorage.setup.RFToolsStorageMessages;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StorageCraftingTools {

    @Nonnull
    private static int[] tryRecipe(PlayerEntity player, CraftingRecipe craftingRecipe, int n, IItemSource itemSource, boolean strictDamage) {
        CraftingInventory workInventory = new CraftingInventory(new Container(null, -1) {
            @Override
            public boolean canInteractWith(PlayerEntity var1) {
                return false;
            }
        }, 3, 3);

        CraftingInventory inventory = craftingRecipe.getInventory();

        int[] missingCount = new int[10];
        IntSet[] hashSets = new IntSet[9];
        for (int i = 0; i < 10; i++) {
            if (i < 9) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    missingCount[i] = stack.getCount() * n;
                    // @todo 1.14 (tags?)
//                    hashSets[i] = new IntSet(OreDictionary.getOreIDs(stack));
                } else {
                    missingCount[i] = 0;
                }
                workInventory.setInventorySlotContents(i, ItemStack.EMPTY);
            } else {
                missingCount[i] = 0;
            }
        }

        for (Pair<IItemKey, ItemStack> pair : itemSource.getItems()) {
            ItemStack input = pair.getValue();
            int size = input.getCount();
            if (!input.isEmpty()) {
                for (int i = 0; i < 9; i++) {
                    if (missingCount[i] > 0) {
                        ItemStack stack = inventory.getStackInSlot(i);
                        if (match(stack, hashSets[i], input, strictDamage)) {
                            if (size > missingCount[i]) {
                                size -= missingCount[i];
                                missingCount[i] = 0;
                            } else {
                                missingCount[i] -= size;
                                size = 0;
                            }
                            workInventory.setInventorySlotContents(i, input.copy());
                        }
                    }
                }
            }
        }

        Optional<ICraftingRecipe> recipe = craftingRecipe.getCachedRecipe(player.getEntityWorld());
        missingCount[9] = recipe.map(r -> r.matches(workInventory, player.getEntityWorld()) ? 0 : 1).orElse(0);

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

    private static List<ItemStack> testAndConsumeCraftingItems(PlayerEntity player, CraftingRecipe craftingRecipe,
                                                               IItemSource itemSource, boolean strictDamage) {
        CraftingInventory workInventory = new CraftingInventory(new Container(null, -1) {
            @Override
            public boolean canInteractWith(PlayerEntity var1) {
                return false;
            }
        }, 3, 3);

        List<Pair<IItemKey, ItemStack>> undo = new ArrayList<>();
        List<ItemStack> result = new ArrayList<>();
        CraftingInventory inventory = craftingRecipe.getInventory();

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                int count = stack.getCount();
                count = findMatchingItems(workInventory, undo, i, stack, count, itemSource, strictDamage);

                if (count > 0) {
                    // Couldn't find all items.
                    undo(player, itemSource, undo);
                    return Collections.emptyList();
                }
            } else {
                workInventory.setInventorySlotContents(i, ItemStack.EMPTY);
            }
        }
        Optional<ICraftingRecipe> recipe = craftingRecipe.getCachedRecipe(player.getEntityWorld());
        return recipe.map(r -> {
            if (!r.matches(workInventory, player.getEntityWorld())) {
                result.clear();
                undo(player, itemSource, undo);
                return result;
            }
            ItemStack stack = r.getCraftingResult(workInventory);
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

    private static boolean match(@Nonnull ItemStack target, @Nonnull IntSet targetIDs, @Nonnull ItemStack input, boolean strictDamage) {
        if (strictDamage) {
            return (target.getItem() == input.getItem() && (target.getDamage() == input.getDamage()));
        } else {
            if (target.getItem() == input.getItem()) {
                return true;
            }

            if (targetIDs.isEmpty()) {
                return false;
            }

            // Try OreDictionary
            // @todo 1.14
//            int[] inputIDs = OreDictionary.getOreIDs(input);
//            for (int id : inputIDs) {
//                if (targetIDs.contains(id)) {
//                    return true;
//                }
//            }
            return false;
        }
    }

    private static int findMatchingItems(CraftingInventory workInventory, List<Pair<IItemKey, ItemStack>> undo, int i,
                                         @Nonnull ItemStack stack,
                                         int count, IItemSource itemSource, boolean strictDamage) {
// @todo 1.14
        //        IntSet stackIDs = new IntSet(OreDictionary.getOreIDs(stack));
        IntSet stackIDs = IntSets.EMPTY_SET;

        for (Pair<IItemKey, ItemStack> pair : itemSource.getItems()) {
            ItemStack input = pair.getValue();
            if (!input.isEmpty()) {
                if (match(stack, stackIDs, input, strictDamage)) {
                    workInventory.setInventorySlotContents(i, input.copy());
                    int ss = count;
                    if (input.getCount() - ss < 0) {
                        ss = input.getCount();
                    }
                    count -= ss;
                    IItemKey key = pair.getKey();
                    ItemStack actuallyExtracted = itemSource.decrStackSize(key, ss);
                    undo.add(Pair.of(key, actuallyExtracted));
                }
            }
            if (count == 0) {
                break;
            }
        }
        return count;
    }

    private static void undo(PlayerEntity player, IItemSource itemSource, List<Pair<IItemKey, ItemStack>> undo) {
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
        player.openContainer.detectAndSendChanges();
    }

    public static void craftItems(PlayerEntity player, int nn, CraftingRecipe craftingRecipe, IItemSource itemSource) {
        Optional<ICraftingRecipe> recipe = craftingRecipe.getCachedRecipe(player.getEntityWorld());
        if (!recipe.isPresent()) {
            // @todo give error?
            return;
        }

        final int[] n = {nn};
        recipe.ifPresent(r -> {

            ItemStack recipeResult = r.getRecipeOutput();
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
                    List<ItemStack> result = testAndConsumeCraftingItems(player, craftingRecipe, itemSource, true);
                    if (result.isEmpty()) {
                        result = testAndConsumeCraftingItems(player, craftingRecipe, itemSource, false);
                        if (result.isEmpty()) {
                            return;
                        }
                    }
                    for (ItemStack stack : result) {
                        if (!player.inventory.addItemStackToInventory(stack)) {
                            player.entityDropItem(stack, 1.05f);
                        }
                    }
                }
            }
        });
    }


    @Nonnull
    public static int[] testCraftItems(PlayerEntity player, int nn, CraftingRecipe craftingRecipe, IItemSource itemSource) {
        Optional<ICraftingRecipe> recipe = craftingRecipe.getCachedRecipe(player.getEntityWorld());
        if (!recipe.isPresent()) {
            // @todo give error?
            return new int[0];
        }

        final int[] n = {nn};
        return recipe.map(r -> {
            ItemStack recipeResult = r.getRecipeOutput();
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

                // First we try the recipe with exact damage. If that works then that's perfect
                // already. Otherwise we try again with non-exact damage. If that turns out
                // not to work then we return the missing items from the exact damage crafting
                // test because that one has more information about what items are really
                // missing
                int[] result = tryRecipe(player, craftingRecipe, n[0], itemSource, true);
                for (int i = 0; i < 10; i++) {
                    if (result[i] > 0) {
                        // Failed
                        int[] result2 = tryRecipe(player, craftingRecipe, n[0], itemSource, false);
                        if (result2[9] == 0) {
                            return result2;
                        } else {
                            return result;
                        }
                    }
                }
                return result;
            } else {
                return new int[0];
            }
        }).orElse(new int[0]);
    }

    public static void craftFromGrid(PlayerEntity player, int count, boolean test, BlockPos pos, DimensionType type) {
//        player.addStat(StatList.CRAFTING_TABLE_INTERACTION);  // @todo 1.14
        int[] testResult = new int[0];
        TileEntity te = WorldTools.getWorld(player.getEntityWorld(), type).getTileEntity(pos);
        if (te instanceof CraftingGridProvider) {
            testResult = ((CraftingGridProvider) te).craft(player, count, test);
        }
        if (testResult.length > 0) {
            RFToolsStorageMessages.INSTANCE.sendTo(new PacketCraftTestResultToClient(testResult), ((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    public static void requestGridSync(PlayerEntity player, BlockPos pos, DimensionType type) {
        World world = WorldTools.getWorld(player.getEntityWorld(), type);
        CraftingGridProvider provider = null;
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof CraftingGridProvider && te instanceof GenericTileEntity) {
            provider = ((CraftingGridProvider) te);
        }
        if (provider != null) {
            RFToolsStorageMessages.INSTANCE.sendTo(new PacketGridToClient(pos, ((GenericTileEntity) te).getDimensionType(), provider.getCraftingGrid()), ((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        }
    }
}
