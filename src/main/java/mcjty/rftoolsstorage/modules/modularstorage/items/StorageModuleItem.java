package mcjty.rftoolsstorage.modules.modularstorage.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.crafting.INBTPreservingIngredient;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbase.api.storage.IStorageModuleItem;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.storage.StorageEntry;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static mcjty.lib.builder.TooltipBuilder.*;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;

public class StorageModuleItem extends Item implements INBTPreservingIngredient, IStorageModuleItem {

    public static final int STORAGE_TIER1 = 0;
    public static final int STORAGE_TIER2 = 1;
    public static final int STORAGE_TIER3 = 2;
    public static final int STORAGE_TIER4 = 3;
    public static final int STORAGE_REMOTE = 6;
    public static final int[] MAXSIZE = new int[]{100, 200, 300, 500, 0, 0, -1};

    private final int tier;

    private final Lazy<TooltipBuilder> tooltipBuilder = () -> new TooltipBuilder()
            .info(header(),
                    parameter("items", stack -> !isRemoteModule() && hasUUID(stack), this::getContentsStringClient),
                    key("message.rftoolsstorage.shiftmessage"))
            .infoShift(header(),
                    gold(stack -> isRemoteModule()),
                    parameter("info", stack -> !(isRemoteModule()), stack -> Integer.toString(getMax())),
                    parameter("remoteid", stack -> isRemoteModule(), stack -> {
                        CompoundTag tag = stack.getTag();
                        if (tag != null && tag.contains("id")) {
                            int id = tag.getInt("id");
                            return Integer.toString(id);
                        } else {
                            return "<unlinked>";
                        }
                    }),
                    parameter("uuid", stack -> {
                        CompoundTag tag = stack.getTag();
                        if (tag != null && tag.hasUUID("uuid")) {
                            return tag.getUUID("uuid").toString();
                        } else {
                            return "<unset>";
                        }
                    }),
                    parameter("version", stack -> {
                        CompoundTag tag = stack.getTag();
                        if (tag != null) {
                            return Integer.toString(tag.getInt("version"));
                        } else {
                            return "<unset>";
                        }
                    }),
                    parameter("items", stack -> !isRemoteModule() && hasUUID(stack), this::getContentsStringClient))
            .infoAdvanced(parameter("advanced", this::getAdvancedInfoClient));

    private String getContentsStringClient(ItemStack stack) {
        StorageEntry storage = getStorageClient(stack);
        if (storage != null) {
            // @todo is this really needed if we only need number of items? Re-evaluate
            NonNullList<ItemStack> stacks = storage.getStacks();
            int cnt = 0;
            for (ItemStack s : stacks) {
                if (!s.isEmpty()) {
                    cnt++;
                }
            }
            return cnt + "/" + getMax();
        }
        return "<unknown>";
    }

    private String getAdvancedInfoClient(ItemStack stack) {
        StorageEntry storage = getStorageClient(stack);
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

    /// Client-side version to get storage
    private StorageEntry getStorageClient(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return null;
        }
        if (tag.hasUUID("uuid")) {
            UUID uuid = tag.getUUID("uuid");
            int version = tag.getInt("version");
            return RFToolsStorage.setup.clientStorageHolder.getStorage(uuid, version);
        } else {
            return null;
        }
    }

    private boolean isRemoteModule() {
        return getMax() == -1;
    }

    private boolean hasUUID(ItemStack stack) {
        if (!stack.hasTag()) {
            return false;
        }
        return stack.getTag().hasUUID("uuid");
    }

    public StorageModuleItem(int tier) {
        super(new Properties()
                .stacksTo(1)
                .durability(0)
                .tab(RFToolsStorage.setup.getTab()));
        this.tier = tier;
    }

    private int getMax() {
        return MAXSIZE[tier];
    }

    @Override
    public void onCraftedBy(@Nonnull ItemStack stack, @Nonnull Level worldIn, @Nonnull Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("createdBy")) {
            tag.putString("createdBy", player.getName().getString());   // @todo 1.16 getFormattedText
        }
    }

    public static UUID getOrCreateUUID(ItemStack stack) {
        if (!(stack.getItem() instanceof StorageModuleItem)) {
            throw new RuntimeException("This is not supposed to happen! Needs to be a storage item!");
        }
        CompoundTag nbt = stack.getOrCreateTag();
        if (!nbt.hasUUID("uuid")) {
            nbt.putUUID("uuid", UUID.randomUUID());
            nbt.putInt("version", 0);   // Make sure the version is not up to date (StorageEntry starts at version 1)
        }
        return nbt.getUUID("uuid");
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
        return List.of("uuid");
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            Logging.message(player, ChatFormatting.YELLOW + "Place this module in a storage module tablet to access contents");
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack itemStack, @Nullable Level worldIn, @Nonnull List<Component> list, @Nonnull TooltipFlag flags) {
        super.appendHoverText(itemStack, worldIn, list, flags);
        tooltipBuilder.get().makeTooltip(new ResourceLocation(RFToolsStorage.MODID, "storage_module"), itemStack, list, flags);
    }

}
