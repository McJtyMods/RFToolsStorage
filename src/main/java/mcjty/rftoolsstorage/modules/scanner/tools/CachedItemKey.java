package mcjty.rftoolsstorage.modules.scanner.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;

public record CachedItemKey(BlockPos pos, Item item, int meta) {
}
