package mcjty.rftoolsstorage.craftinggrid;

import mcjty.lib.network.CustomPacketPayload;
import mcjty.lib.network.PlayPayloadContext;
import mcjty.rftoolsstorage.RFToolsStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public record PacketCraftTestResultToClient(List<Pair<ItemStack, Integer>> testResult) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(RFToolsStorage.MODID, "crafttestresult");

    public static PacketCraftTestResultToClient create(List<Pair<ItemStack, Integer>> testResult) {
        return new PacketCraftTestResultToClient(testResult);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(testResult.size());
        for (Pair<ItemStack, Integer> pair : testResult) {
            buf.writeItemStack(pair.getLeft(), false);
            buf.writeInt(pair.getRight());
        }
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static PacketCraftTestResultToClient create(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<Pair<ItemStack, Integer>> testResult = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            testResult.add(Pair.of(buf.readItem(), buf.readInt()));
        }
        return new PacketCraftTestResultToClient(testResult);
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            GuiCraftingGrid.testResultFromServer = testResult;
        });
    }

}