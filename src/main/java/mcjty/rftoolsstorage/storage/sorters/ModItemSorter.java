package mcjty.rftoolsstorage.storage.sorters;

import mcjty.lib.varia.Tools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;

public class ModItemSorter implements ItemSorter {
    @Override
    public String getName() {
        return "mod";
    }

    @Override
    public String getTooltip() {
        return "Sort on mod";
    }

    @Override
    public int getU() {
        return 15*16;
    }

    @Override
    public int getV() {
        return 0;
    }

    @Override
    public Comparator<Pair<ItemStack, Integer>> getComparator() {
        return ModItemSorter::compareMod;
    }

    @Override
    public boolean isSameGroup(Pair<ItemStack, Integer> o1, Pair<ItemStack, Integer> o2) {
        String name1 = getMod(o1);
        String name2 = getMod(o2);
        return name1.equals(name2);
    }

    public static int compareMod(Pair<ItemStack, Integer> o1, Pair<ItemStack, Integer> o2) {
        String name1 = getMod(o1);
        String name2 = getMod(o2);

        if (name1.equals(name2)) {
            return NameItemSorter.compareNames(o1, o2);
        }
        return name1.compareTo(name2);
    }

    public static String getModidForBlock(Block block) {
        ResourceLocation nameForObject = Tools.getId(block);
        if (nameForObject == null) {
            return "?";
        }
        return nameForObject.getNamespace();
    }

    public static String getModidForItem(Item item) {
        ResourceLocation nameForObject = Tools.getId(item);
        if (nameForObject == null) {
            return "?";
        }
        return nameForObject.getNamespace();
    }


    private static String getMod(Pair<ItemStack, Integer> object) {
        ItemStack stack = object.getKey();
        return getMod(stack);
    }

    public static String getMod(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();
            return getModidForBlock(block);
        } else {
            return getModidForItem(item);
        }
    }

    @Override
    public String getGroupName(Pair<ItemStack, Integer> object) {
        return getMod(object);
    }
}
