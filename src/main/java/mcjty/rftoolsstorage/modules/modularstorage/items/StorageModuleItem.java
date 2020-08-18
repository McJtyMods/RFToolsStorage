package mcjty.rftoolsstorage.modules.modularstorage.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.crafting.INBTPreservingIngredient;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbase.api.storage.IStorageModuleItem;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.storage.StorageEntry;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static mcjty.lib.builder.TooltipBuilder.*;

public class StorageModuleItem extends Item implements INBTPreservingIngredient, IStorageModuleItem {

    public static final int STORAGE_TIER1 = 0;
    public static final int STORAGE_TIER2 = 1;
    public static final int STORAGE_TIER3 = 2;
    public static final int STORAGE_TIER4 = 3;
    public static final int STORAGE_REMOTE = 6;
    public static final int MAXSIZE[] = new int[]{100, 200, 300, 500, 0, 0, -1};

    private final int tier;

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header(),
                    parameter("items", stack -> !isRemoteModule() && hasUUID(stack), this::getContentsString),
                    key("message.rftoolsstorage.shiftmessage"))
            .infoShift(header(),
                    gold(stack -> isRemoteModule()),
                    parameter("info", stack -> !(isRemoteModule()), stack -> Integer.toString(getMax())),
                    parameter("remoteid", stack -> isRemoteModule(), stack -> {
                        CompoundNBT tag = stack.getTag();
                        if (tag != null && tag.contains("id")) {
                            int id = tag.getInt("id");
                            return Integer.toString(id);
                        } else {
                            return "<unlinked>";
                        }
                    }),
                    parameter("uuid", stack -> {
                        CompoundNBT tag = stack.getTag();
                        if (tag != null) {
                            return tag.getUniqueId("uuid").toString();
                        } else {
                            return "<unset>";
                        }
                    }),
                    parameter("version", stack -> {
                        CompoundNBT tag = stack.getTag();
                        if (tag != null) {
                            return Integer.toString(tag.getInt("version"));
                        } else {
                            return "<unset>";
                        }
                    }),
                    parameter("items", stack -> !isRemoteModule() && hasUUID(stack), this::getContentsString))
            .infoAdvanced(parameter("advanced", this::getAdvancedInfo));

    private String getContentsString(ItemStack stack) {
        StorageEntry storage = getStorage(stack);
        if (storage != null) {
            // @todo is this really needed if we only need number of items? Re-evaluate
            NonNullList<ItemStack> stacks = storage.getStacks();
            int cnt = 0;
            for (ItemStack s : stacks) {
                if (!s.isEmpty()) {
                    cnt++;
                }
            }
            return Integer.toString(cnt) + "/" + Integer.toString(getMax());
        }
        return "<unknown>";
    }

    private String getAdvancedInfo(ItemStack stack) {
        StorageEntry storage = getStorage(stack);
        if (storage != null) {
            String createdBy = storage.getCreatedBy();
            String info = "";
            if (createdBy != null && !createdBy.isEmpty()) {
                info += "Created by " + createdBy;
            } else {
                info += "Unknown creator";
            }
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            Date creationTime = new Date(storage.getCreationTime());
            Date updateTime = new Date(storage.getUpdateTime());
            info += ", Creation time " + dateFormat.format(creationTime);
            info += ", Update time " + dateFormat.format(updateTime);
            return info;
        }
        return "<unknown>";

    }

    private StorageEntry getStorage(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag == null) {
            return null;
        }
        UUID uuid = tag.getUniqueId("uuid");
        int version = tag.getInt("version");
        return RFToolsStorage.setup.clientStorageHolder.getStorage(uuid, version);
    }

    private boolean isRemoteModule() {
        return getMax() == -1;
    }

    private boolean hasUUID(ItemStack stack) {
        if (!stack.hasTag()) {
            return false;
        }
        return stack.getTag().hasUniqueId("uuid");
    }

    public StorageModuleItem(int tier) {
        super(new Properties()
                .maxStackSize(1)
                .maxDamage(0)
                .group(RFToolsStorage.setup.getTab()));
        this.tier = tier;
    }

    private int getMax() {
        return MAXSIZE[tier];
    }

    @Override
    public void onCreated(ItemStack stack, World worldIn, PlayerEntity player) {
        if (player != null) {
            CompoundNBT tag = stack.getOrCreateTag();
            if (!tag.contains("createdBy")) {
                tag.putString("createdBy", player.getName().getString());   // @todo 1.16 getFormattedText
            }
        }
    }


    public static UUID getOrCreateUUID(ItemStack stack) {
        if (!(stack.getItem() instanceof StorageModuleItem)) {
            throw new RuntimeException("This is not supposed to happen! Needs to be a storage item!");
        }
        CompoundNBT nbt = stack.getOrCreateTag();
        if (!nbt.hasUniqueId("uuid")) {
            nbt.putUniqueId("uuid", UUID.randomUUID());
            nbt.putInt("version", 0);   // Make sure the version is not up to date (StorageEntry starts at version 1)
        }
        return nbt.getUniqueId("uuid");
    }

    public static String getCreatedBy(ItemStack storageCard) {
        if (storageCard.hasTag()) {
            return storageCard.getTag().getString("createdBy");
        }
        return null;
    }


    public static int getVersion(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getInt("version");
        } else {
            return 0;
        }
    }

    public static int getSize(ItemStack storageCard) {
        if (storageCard.getItem() instanceof StorageModuleItem) {
            int tier = ((StorageModuleItem) storageCard.getItem()).tier;
            return MAXSIZE[tier];
        }
        return 0;
    }

    @Override
    public Collection<String> getTagsToPreserve() {
        return Arrays.asList("uuidMost", "uuidLeast");
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
        tooltipBuilder.makeTooltip(new ResourceLocation(RFToolsStorage.MODID, "storage_module"), itemStack, list, flags);
    }

}
