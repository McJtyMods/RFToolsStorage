package mcjty.rftoolsstorage.craftinggrid;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketCraftTestResultToClient {

    private int[] testResult;

    public void toBytes(FriendlyByteBuf buf) {
        for (int i = 0; i < 10; i++) {
            buf.writeInt(testResult[i]);
        }
    }

    public PacketCraftTestResultToClient() {
    }

    public PacketCraftTestResultToClient(FriendlyByteBuf buf) {
        testResult = new int[10];
        for (int i = 0; i < 10; i++) {
            testResult[i] = buf.readInt();
        }
    }

    public PacketCraftTestResultToClient(int[] testResult) {
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