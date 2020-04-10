package mcjty.rftoolsstorage.modules.scanner.tools;

import mcjty.lib.varia.ItemStackList;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class ItemFilterCache {
    private boolean matchDamage = true;
    private boolean blacklistMode = true;
    private boolean nbtMode = false;
    private ItemStackList stacks;

    public ItemFilterCache(boolean matchDamage, boolean blacklistMode, boolean nbtMode, @Nonnull ItemStackList stacks) {
        this.matchDamage = matchDamage;
        this.blacklistMode = blacklistMode;
        this.nbtMode = nbtMode;
        this.stacks = stacks;
    }

    public boolean match(ItemStack stack) {
        if (!stack.isEmpty()) {
            boolean match = itemMatches(stack);
            return match != blacklistMode;
        }
        return false;
    }

    private boolean itemMatches(ItemStack stack) {
        if (stacks != null) {
            for (ItemStack itemStack : stacks) {
                if (matchDamage && itemStack.getDamage() != stack.getDamage()) {    // @todo 1.14, used to be meta. Check!
                    continue;
                }
                if (nbtMode && !ItemStack.areItemStackTagsEqual(itemStack, stack)) {
                    continue;
                }
                if (itemStack.getItem().equals(stack.getItem())) {
                    return true;
                }
            }
        }
        return false;
    }
}