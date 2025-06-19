package mcjty.rftoolsstorage.compat.jei;

import mcjty.lib.varia.ItemStackList;
import mcjty.rftoolsbase.api.compat.JEIRecipeAcceptor;
import mcjty.rftoolsbase.modules.tablet.items.TabletItem;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;


public record PacketSendRecipe(List<ItemStack> stacks, BlockPos pos) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsStorage.MODID, "sendrecipe");
    public static final CustomPacketPayload.Type<PacketSendRecipe> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSendRecipe> CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_LIST_STREAM_CODEC, PacketSendRecipe::stacks,
            BlockPos.STREAM_CODEC, PacketSendRecipe::pos,
            PacketSendRecipe::new
    );

    public static PacketSendRecipe create(ItemStackList items, BlockPos pos) {
        return new PacketSendRecipe(items, pos);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            Level world = player.getCommandSenderWorld();
            if (pos == null) {
                // Handle tablet version
                ItemStack mainhand = player.getMainHandItem();
                if (!mainhand.isEmpty() && mainhand.getItem() instanceof TabletItem) {
                    if (player.containerMenu instanceof StorageScannerContainer) {
                        StorageScannerContainer container = (StorageScannerContainer) player.containerMenu;
//                        container
//                        tabletContainer
                        // @todo
                    }
//                        ModularStorageItemContainer storageItemContainer = (ModularStorageItemContainer) player.openContainer;
//                        storageItemContainer.getJEIRecipeAcceptor().setGridContents(stacks);
//                    } else if (player.openContainer instanceof RemoteStorageItemContainer) {
//                        RemoteStorageItemContainer storageItemContainer = (RemoteStorageItemContainer) player.openContainer;
//                        storageItemContainer.getJEIRecipeAcceptor().setGridContents(stacks);
//                    } else if (player.openContainer instanceof StorageScannerContainer) {
//                        StorageScannerContainer storageItemContainer = (StorageScannerContainer) player.openContainer;
//                        storageItemContainer.getStorageScannerTileEntity().setGridContents(stacks);
//                    }
                }
            } else {
                BlockEntity te = world.getBlockEntity(pos);
                if (te instanceof JEIRecipeAcceptor) {
                    JEIRecipeAcceptor acceptor = (JEIRecipeAcceptor) te;
                    acceptor.setGridContents(stacks);
                }
            }
        });
    }
}
