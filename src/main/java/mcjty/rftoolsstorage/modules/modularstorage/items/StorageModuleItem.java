package mcjty.rftoolsstorage.modules.modularstorage.items;

import mcjty.lib.McJtyLib;
import mcjty.lib.crafting.INBTPreservingIngredient;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.storage.StorageEntry;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class StorageModuleItem extends Item implements INBTPreservingIngredient {

    public static final int STORAGE_TIER1 = 0;
    public static final int STORAGE_TIER2 = 1;
    public static final int STORAGE_TIER3 = 2;
    public static final int STORAGE_TIER4 = 3;
    public static final int STORAGE_REMOTE = 6;
    public static final int MAXSIZE[] = new int[] { 100, 200, 300, 500, 0, 0, -1 };

    private final int tier;

    public StorageModuleItem(int tier) {
        super(new Properties()
                .maxStackSize(1)
                .maxDamage(0)
                .group(RFToolsStorage.setup.getTab()));
        setRegistryName("storage_module" + tier);
        this.tier = tier;
    }

//    @Override
//    public void onCreated(ItemStack stack, World worldIn, PlayerEntity playerIn) {
//        System.out.println("StorageModuleItem.onCreated");
//        getOrCreateUUID(stack);
//    }

    public static UUID getOrCreateUUID(ItemStack stack) {
        if (!(stack.getItem() instanceof StorageModuleItem)) {
            throw new RuntimeException("This is not supposed to happen! Needs to be a storage item!");
        }
        CompoundNBT nbt = stack.getOrCreateTag();
        if (!nbt.contains("uuidMost")) {
            nbt.putUniqueId("uuid", UUID.randomUUID());
            nbt.putInt("version", 0);   // Make sure the version is not up to date (StorageEntry starts at version 1)
        }
        return nbt.getUniqueId("uuid");
    }

    @Override
    public Collection<String> getTagsToPreserve() {
        // @todo when upgrading we need to see how to upgrade the storage entry
        return Arrays.asList("");   // @todo 1.14
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            Logging.message(player, TextFormatting.YELLOW + "Place this module in a storage module tablet to access contents");
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    // Called from the Remote or Modular store TE's to update the stack size for this item while it is inside that TE.
//    public static void updateStackSize(ItemStack stack, int numStacks) {
//        if (stack.isEmpty()) {
//            return;
//        }
//        NBTTagCompound tagCompound = stack.getTagCompound();
//        if (tagCompound == null) {
//            tagCompound = new NBTTagCompound();
//            stack.setTagCompound(tagCompound);
//        }
//        tagCompound.setInteger("count", numStacks);
//    }


    @Override
    public void addInformation(ItemStack itemStack, @Nullable World worldIn, List<ITextComponent> list, ITooltipFlag flags) {
        super.addInformation(itemStack, worldIn, list, flags);
        int max = MAXSIZE[tier];
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            addModuleInformation(itemStack, list, max, tagCompound);
        }
        if (McJtyLib.proxy.isShiftKeyDown()) {
            list.add(new StringTextComponent(TextFormatting.WHITE + "This storage module is for the Modular Storage block."));
            if (max == -1) {
                list.add(new StringTextComponent(TextFormatting.WHITE + "This module supports a remote inventory."));
                list.add(new StringTextComponent(TextFormatting.WHITE + "Link to another storage module in the remote storage block."));
            } else {
                list.add(new StringTextComponent(TextFormatting.WHITE + "This module supports " + max + " stacks"));
            }
            if (tagCompound != null) {
                list.add(new StringTextComponent(TextFormatting.BLUE + "UUID: " + tagCompound.getUniqueId("uuid").toString()));
                list.add(new StringTextComponent(TextFormatting.BLUE + "version: " + tagCompound.getInt("version")));
            }
        } else {
            list.add(new StringTextComponent(TextFormatting.WHITE + RFToolsStorage.SHIFT_MESSAGE));
        }
    }

    public static void addModuleInformation(ItemStack stack, List<ITextComponent> list, int max, CompoundNBT tagCompound) {
        if (max == -1) {
            // @todo 1.14
            // This is a remote storage module.
            if (tagCompound.contains("id")) {
                int id = tagCompound.getInt("id");
                list.add(new StringTextComponent(TextFormatting.GREEN + "Remote id: " + id));
            } else {
                list.add(new StringTextComponent(TextFormatting.YELLOW + "Unlinked"));
            }
        } else if (tagCompound.contains("uuidMost")) {
            UUID uuid = tagCompound.getUniqueId("uuid");
            int version = tagCompound.getInt("version");
            StorageEntry storage = RFToolsStorage.setup.clientStorageHolder.getStorage(uuid, version);
            if (storage != null) {
                // @todo is this really needed if we only need number of items? Re-evaluate
                NonNullList<ItemStack> stacks = storage.getStacks();
                int cnt = 0;
                for (ItemStack s : stacks) {
                    if (!s.isEmpty()) {
                        cnt++;
                    }
                }
                list.add(new StringTextComponent(TextFormatting.GREEN + "Contents: " + cnt + "/" + max + " stacks"));
            }
        }
    }
}
