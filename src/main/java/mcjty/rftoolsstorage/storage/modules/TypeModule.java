package mcjty.rftoolsstorage.storage.modules;

import mcjty.rftoolsstorage.storage.sorters.ItemSorter;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface TypeModule {
    List<ItemSorter> getSorters();

    String getLongLabel(ItemStack stack);

    String getShortLabel(ItemStack stack);
}
