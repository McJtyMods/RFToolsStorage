package mcjty.rftoolsstorage.craftinggrid;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class PacketCraftTestResultToClient {

    private final List<Pair<ItemStack, Integer>> testResult;

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(testResult.size());
        for (Pair<ItemStack, Integer> pair : testResult) {
            buf.writeItemStack(pair.getLeft(), false);
            buf.writeInt(pair.getRight());
        }
    }

    public PacketCraftTestResultToClient(FriendlyByteBuf buf) {
        int size = buf.readInt();
        testResult = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            testResult.add(Pair.of(buf.readItem(), buf.readInt()));
        }
    }

    public PacketCraftTestResultToClient(List<Pair<ItemStack, Integer>> testResult) {
        this.testResult = testResult;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            GuiCraftingGrid.testResultFromServer = testResult;
        });
        ctx.setPacketHandled(true);
    }

}