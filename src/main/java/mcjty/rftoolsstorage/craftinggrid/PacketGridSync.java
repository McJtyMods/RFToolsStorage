package mcjty.rftoolsstorage.craftinggrid;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class PacketGridSync {

    private BlockPos pos;
    private List<ItemStack[]> recipes;

    public void convertFromBytes(PacketBuffer buf) {
        if (buf.readBoolean()) {
            pos = buf.readBlockPos();
        } else {
            pos = null;
        }
        int s = buf.readInt();
        recipes = new ArrayList<>(s);
        for (int i = 0 ; i < s ; i++) {
            int ss = buf.readInt();
            ItemStack[] stacks = new ItemStack[ss];
            for (int j = 0 ; j < ss ; j++) {
                stacks[j] = buf.readItemStack();
            }
            recipes.add(stacks);
        }
    }

    public void convertToBytes(PacketBuffer buf) {
        if (pos != null) {
            buf.writeBoolean(true);
            buf.writeBlockPos(pos);
        } else {
            buf.writeBoolean(false);
        }
        buf.writeInt(recipes.size());
        for (ItemStack[] recipe : recipes) {
            buf.writeInt(recipe.length);
            for (ItemStack stack : recipe) {
                buf.writeItemStack(stack);
            }
        }
    }

    protected void init(BlockPos pos, CraftingGrid grid) {
        this.pos = pos;
        recipes = new ArrayList<>();
        for (int i = 0 ; i < 6 ; i++) {
            CraftingRecipe recipe = grid.getRecipe(i);
            CraftingInventory inventory = recipe.getInventory();
            ItemStack[] stacks = new ItemStack[10];
            stacks[0] = recipe.getResult();
            for (int j = 0 ; j < 9 ; j++) {
                stacks[j+1] = inventory.getStackInSlot(j);
            }
            recipes.add(stacks);
        }
    }

    protected CraftingGridProvider handleMessage(World world, PlayerEntity player) {
        CraftingGridProvider provider = null;
        if (pos == null) {
            // Handle tablet version
            ItemStack mainhand = player.getHeldItemMainhand();
            // @todo 1.14
//            if (!mainhand.isEmpty() && mainhand.getItem() == ModularStorageSetup.storageModuleTabletItem) {
//                if (player.openContainer instanceof ModularStorageItemContainer) {
//                    ModularStorageItemContainer storageItemContainer = (ModularStorageItemContainer) player.openContainer;
//                    provider = storageItemContainer.getCraftingGridProvider();
//                } else if (player.openContainer instanceof RemoteStorageItemContainer) {
//                    RemoteStorageItemContainer storageItemContainer = (RemoteStorageItemContainer) player.openContainer;
//                    provider = storageItemContainer.getCraftingGridProvider();
//                } else if (player.openContainer instanceof StorageScannerContainer) {
//                    StorageScannerContainer storageItemContainer = (StorageScannerContainer) player.openContainer;
//                    provider = storageItemContainer.getStorageScannerTileEntity();
//                }
//            }
        } else {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof CraftingGridProvider) {
                provider = ((CraftingGridProvider) te);
            }
        }
        if (provider != null) {
            for (int i = 0; i < recipes.size(); i++) {
                provider.setRecipe(i, recipes.get(i));
            }
        }
        return provider;
    }
}
