package mcjty.rftoolsstorage.modules.scanner.network;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.modules.scanner.blocks.StorageScannerTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public record PacketRequestItem(ResourceKey<Level> dimensionId, BlockPos pos, BlockPos inventoryPos, ItemStack item, Integer amount, Boolean craftable) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsStorage.MODID, "requestitem");

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(dimensionId.location());
        buf.writeBlockPos(pos);
        buf.writeBlockPos(inventoryPos);
        NetworkTools.writeItemStack(buf, item);
        buf.writeInt(amount);
        buf.writeBoolean(craftable);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static PacketRequestItem create(FriendlyByteBuf buf) {
        return new PacketRequestItem(LevelTools.getId(buf.readResourceLocation()), buf.readBlockPos(), buf.readBlockPos(), NetworkTools.readItemStack(buf), buf.readInt(), buf.readBoolean());
    }

    public static PacketRequestItem create(ResourceKey<Level>
                                     dimensionId, BlockPos pos, BlockPos inventoryPos, ItemStack item, int amount, boolean craftable) {
        return new PacketRequestItem(dimensionId, pos, inventoryPos, item, amount, craftable);
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(player -> {
                Level world = LevelTools.getLevel(player.level(), dimensionId);
                if (world == null) {
                    return;
                }
                if (!LevelTools.isLoaded(world, pos)) {
                    return;
                }
                BlockEntity te = world.getBlockEntity(pos);
                if (te instanceof StorageScannerTileEntity scanner) {
                    if (craftable) {
                        scanner.requestCraft(inventoryPos, item, amount, player);
                    } else {
                        scanner.requestStack(inventoryPos, item, amount, player);
                    }
                }
            });
        });
    }
}
