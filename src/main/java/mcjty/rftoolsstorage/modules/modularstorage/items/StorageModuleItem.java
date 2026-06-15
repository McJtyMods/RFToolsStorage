package mcjty.rftoolsstorage.modules.modularstorage.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.crafting.IComponentsToPreserve;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbase.api.storage.IStorageModuleItem;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.modularstorage.ModularStorageModule;
import mcjty.rftoolsstorage.modules.modularstorage.data.StorageModuleData;
import mcjty.rftoolsstorage.storage.StorageInfo;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.gui.ManualEntry;
import mcjty.rftoolsbase.tools.ManualHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.Lazy;

import javax.annotation.Nonnull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static mcjty.lib.builder.TooltipBuilder.*;

public class StorageModuleItem extends Item implements IComponentsToPreserve, IStorageModuleItem, ITooltipSettings {

    public static final int STORAGE_TIER1 = 0;
    public static final int STORAGE_TIER2 = 1;
    public static final int STORAGE_TIER3 = 2;
    public static final int STORAGE_TIER4 = 3;
    public static final int STORAGE_REMOTE = 6;
    public static final int[] MAXSIZE = new int[]{100, 200, 300, 500, 0, 0, -1};

    private final int tier;

    private final Lazy<TooltipBuilder> tooltipBuilder = Lazy.of(() -> new TooltipBuilder()
            .info(header(),
                    parameter("items", stack -> !isRemoteModule() && hasUUID(stack), this::getContentsStringClient),
                    key("message.rftoolsstorage.shiftmessage"))
            .infoShift(header(),
                    gold(stack -> isRemoteModule()),
                    parameter("info", stack -> !(isRemoteModule()), stack -> Integer.toString(getMax())),
                    parameter("remoteid", stack -> isRemoteModule(), stack -> {
                        int id = getData(stack).id();
                        if (id != -1) {
                            return Integer.toString(id);
                        } else {
                            return "<unlinked>";
                        }
                    }),
                    parameter("uuid", stack -> {
                        UUID uuid = getData(stack).uuid();
                        if (uuid != null) {
                            return uuid.toString();
                        } else {
                            return "<unset>";
                        }
                    }),
                    parameter("version", stack -> {
                        int version = getData(stack).version();
                        if (version != -1) {
                            return Integer.toString(version);
                        } else {
                            return "<unset>";
                        }
                    }),
                    parameter("items", stack -> !isRemoteModule() && hasUUID(stack), this::getContentsStringClient))
            .infoAdvanced(parameter("advanced", this::getAdvancedInfoClient)));

    private String getContentsStringClient(ItemStack stack) {
        int cnt = getData(stack).infoAmount();
        if (cnt != -1) {
            return cnt + "/" + getMax();
        }
        return "<unknown>";
    }

    public static StorageModuleData getData(ItemStack stack) {
        StorageModuleData data = stack.get(ModularStorageModule.ITEM_STORAGE_MODULE_DATA.get());
        if (data == null) {
            data = StorageModuleData.DEFAULT;
        }
        return data;
    }

    private String getAdvancedInfoClient(ItemStack stack) {
        StorageInfo storage = getStorageClient(stack);
        if (storage != null) {
            String createdBy = storage.createdBy();
            String info = "";
            if (createdBy != null && !createdBy.isEmpty()) {
                info += "Created by " + createdBy;
            } else {
                info += "Unknown creator";
            }
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            Date creationTime = new Date(getData(stack).creationTime());
            Date updateTime = new Date(getData(stack).updateTime());
            info += ", Creation time " + dateFormat.format(creationTime);
            info += ", Update time " + dateFormat.format(updateTime);
            return info;
        }
        return "<unknown>";

    }

    /// Client-side version to get storage
    private StorageInfo getStorageClient(ItemStack stack) {
        StorageModuleData data = getData(stack);
        if (data.id() == -1) {
            return null;
        }
        return getStorageInfo(stack);
    }

    @Nonnull
    public static StorageInfo getStorageInfo(ItemStack storageCard) {
        Item item = storageCard.getItem();
        if (item instanceof StorageModuleItem) {
            UUID uuid = StorageModuleItem.getOrCreateUUID(storageCard);
            int version = StorageModuleItem.getVersion(storageCard);
            int size = StorageModuleItem.getSize(storageCard);
            String createdBy = StorageModuleItem.getCreatedBy(storageCard);
            return new StorageInfo(uuid, version, size, createdBy);
        } else {
            return StorageInfo.EMPTY;
        }
    }

    private boolean isRemoteModule() {
        return getMax() == -1;
    }

    private boolean hasUUID(ItemStack stack) {
        UUID uuid = getData(stack).uuid();
        return uuid != null;
    }

    public StorageModuleItem(int tier) {
        super(RFToolsStorage.setup.defaultProperties()
                .stacksTo(1)
                .durability(0));
        this.tier = tier;
    }

    private int getMax() {
        return MAXSIZE[tier];
    }

    @Override
    public void onCraftedBy(@Nonnull ItemStack stack, @Nonnull Level worldIn, @Nonnull Player player) {
        StorageModuleData data = getData(stack);
        String createdBy = data.createdBy();
        if (createdBy != null && !createdBy.isEmpty()) {
            data = data.withCreatedBy(player.getName().getString());
            stack.set(ModularStorageModule.ITEM_STORAGE_MODULE_DATA.get(), data);
        }
    }

    public static UUID getOrCreateUUID(ItemStack stack) {
        if (!(stack.getItem() instanceof StorageModuleItem)) {
            throw new RuntimeException("This is not supposed to happen! Needs to be a storage item!");
        }
        StorageModuleData data = getData(stack);
        UUID uuid = data.uuid();
        if (uuid == null) {
            uuid = UUID.randomUUID();
            // Make sure the version is not up to date (StorageEntry starts at version 1)
            data = data.withUuid(uuid).withVersion(0);
            stack.set(ModularStorageModule.ITEM_STORAGE_MODULE_DATA.get(), data);
        }
        return uuid;
    }

    public static String getCreatedBy(ItemStack storageCard) {
        return getData(storageCard).createdBy();
    }


    public static int getVersion(ItemStack stack) {
        return getData(stack).version();
    }

    public static int getSize(ItemStack storageCard) {
        if (storageCard.getItem() instanceof StorageModuleItem) {
            int tier = ((StorageModuleItem) storageCard.getItem()).tier;
            return MAXSIZE[tier];
        }
        return 0;
    }

    @Override
    public Collection<DataComponentType<?>> getComponentsToPreserve() {
        return List.of(ModularStorageModule.ITEM_STORAGE_MODULE_DATA.get());
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
    public void appendHoverText(ItemStack itemStack, TooltipContext context, List<Component> list, TooltipFlag flags) {
        super.appendHoverText(itemStack, context, list, flags);
        tooltipBuilder.get().makeTooltip(ResourceLocation.fromNamespaceAndPath(RFToolsStorage.MODID, "storage_module"), itemStack, list, flags);
    }


    @Override
    public ManualEntry getManualEntry() {
        if (isRemoteModule()) {
            return ManualHelper.create("rftoolsstorage:modularstorage/remote_module");
        }
        return ManualHelper.create("rftoolsstorage:modularstorage/storagemodules");
    }

}
