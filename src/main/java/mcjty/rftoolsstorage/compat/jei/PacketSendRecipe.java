package mcjty.rftoolsstorage.compat.jei;

import mcjty.lib.varia.ItemStackList;
import mcjty.rftoolsbase.api.compat.JEIRecipeAcceptor;
import mcjty.rftoolsbase.modules.tablet.items.TabletItem;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;


public class PacketSendRecipe {
    private ItemStackList stacks;
    private BlockPos pos;

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(stacks.size());
        for (ItemStack stack : stacks) {
            buf.writeItem(stack);
        }
        if (pos != null) {
            buf.writeBoolean(true);
            buf.writeBlockPos(pos);
        } else {
            buf.writeBoolean(false);
        }
    }

    public PacketSendRecipe() {
    }

    public PacketSendRecipe(PacketBuffer buf) {
        int l = buf.readInt();
        stacks = ItemStackList.create(l);
        for (int i = 0 ; i < l ; i++) {
            stacks.set(i, buf.readItem());
        }
        if (buf.readBoolean()) {
            pos = buf.readBlockPos();
        } else {
            pos = null;
        }
    }

    public PacketSendRecipe(ItemStackList stacks, BlockPos pos) {
        this.stacks = stacks;
        this.pos = pos;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity player = ctx.getSender();
            World world = player.getCommandSenderWorld();
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
                TileEntity te = world.getBlockEntity(pos);
                if (te instanceof JEIRecipeAcceptor) {
                    JEIRecipeAcceptor acceptor = (JEIRecipeAcceptor) te;
                    acceptor.setGridContents(stacks);
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}