package mcjty.rftoolsstorage.storage;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A list of items that can be coupled to a storage card
 */
public class StorageEntry {

    private NonNullList<ItemStack> stacks;
    private final UUID uuid;
    private int version;

    public StorageEntry(CompoundNBT nbt) {
        int size = nbt.getInt("slots");
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        ListNBT tagList = nbt.getList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundNBT itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");

            if (slot >= 0 && slot < stacks.size()) {
                stacks.set(slot, ItemStack.read(itemTags));
            }
        }

        uuid = nbt.getUniqueId("UUID");
        version = nbt.getInt("version");
    }

    public StorageEntry(int size, UUID uuid) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        this.uuid = uuid;
        this.version = 1;
    }

    public int getVersion() {
        return version;
    }

    public UUID getUuid() {
        return uuid;
    }

    public NonNullList<ItemStack> getStacks() {
        return stacks;
    }

    public CompoundNBT write() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("slots", stacks.size());
        nbt.putInt("version", version);

        ListNBT nbtTagList = new ListNBT();
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty()) {
                CompoundNBT itemTag = new CompoundNBT();
                itemTag.putInt("Slot", i);
                stacks.get(i).write(itemTag);
                nbtTagList.add(itemTag);
            }
        }
        nbt.put("Items", nbtTagList);
        nbt.putUniqueId("UUID", uuid);
        return nbt;
    }

    // Note: when resizing to a smaller size items might be lost
    public void resize(int size) {
        List<ItemStack> oldList = stacks;
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int i = 0 ; i < Math.min(oldList.size(), size) ; i++) {
            stacks.set(i, oldList.get(i));
        }
    }
}
