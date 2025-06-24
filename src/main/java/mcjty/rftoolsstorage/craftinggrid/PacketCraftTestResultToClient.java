package mcjty.rftoolsstorage.craftinggrid;

import mcjty.rftoolsstorage.RFToolsStorage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public record PacketCraftTestResultToClient(List<Pair<ItemStack, Integer>> testResult) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RFToolsStorage.MODID, "crafttestresult");
    public static final CustomPacketPayload.Type<PacketCraftTestResultToClient> TYPE = new CustomPacketPayload.Type<>(ID);

    private static final StreamCodec<RegistryFriendlyByteBuf, Pair<ItemStack, Integer>> PAIR_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, Pair::getLeft, ByteBufCodecs.INT, Pair::getRight, Pair::of);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketCraftTestResultToClient> CODEC = StreamCodec.composite(
            PAIR_CODEC.apply(ByteBufCodecs.list()), PacketCraftTestResultToClient::testResult,
            PacketCraftTestResultToClient::new
    );

    public static PacketCraftTestResultToClient create(List<Pair<ItemStack, Integer>> testResult) {
        return new PacketCraftTestResultToClient(testResult);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            GuiCraftingGrid.testResultFromServer = testResult;
        });
    }

}