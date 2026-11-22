package mcjty.rftoolsstorage.setup;

import mcjty.lib.network.PayloadContext;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import mcjty.lib.network.Networking;
import mcjty.lib.network.PacketSendClientCommand;
import mcjty.lib.network.PacketSendServerCommand;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.compat.jei.PacketSendRecipe;
import mcjty.rftoolsstorage.craftinggrid.PacketCraftTestResultToClient;
import mcjty.rftoolsstorage.craftinggrid.PacketGridToClient;
import mcjty.rftoolsstorage.craftinggrid.PacketGridToServer;
import mcjty.rftoolsstorage.modules.modularstorage.network.PacketStorageInfoToClient;
import mcjty.rftoolsstorage.modules.scanner.network.PacketGetInventoryInfo;
import mcjty.rftoolsstorage.modules.scanner.network.PacketRequestItem;
import mcjty.rftoolsstorage.modules.scanner.network.PacketReturnInventoryInfo;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import javax.annotation.Nonnull;

public class RFToolsStorageMessages {

    public static void registerMessages() {
        PayloadTypeRegistry.serverboundPlay().register(PacketGridToServer.TYPE, PacketGridToServer.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketGridToServer.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.serverboundPlay().register(PacketSendRecipe.TYPE, PacketSendRecipe.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketSendRecipe.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.serverboundPlay().register(PacketGetInventoryInfo.TYPE, PacketGetInventoryInfo.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketGetInventoryInfo.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.serverboundPlay().register(PacketRequestItem.TYPE, PacketRequestItem.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketRequestItem.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.clientboundPlay().register(PacketGridToClient.TYPE, PacketGridToClient.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PacketStorageInfoToClient.TYPE, PacketStorageInfoToClient.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PacketReturnInventoryInfo.TYPE, PacketReturnInventoryInfo.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PacketCraftTestResultToClient.TYPE, PacketCraftTestResultToClient.CODEC);
    }

    public static void sendToServer(String command, @Nonnull TypedMap.Builder argumentBuilder) {
        Networking.sendToServer(new PacketSendServerCommand(RFToolsStorage.MODID, command, argumentBuilder.build()));
    }

    public static void sendToClient(Player player, String command, @Nonnull TypedMap.Builder argumentBuilder) {
        Networking.sendToPlayer(new PacketSendClientCommand(RFToolsStorage.MODID, command, argumentBuilder.build()), player);
    }

    public static <T extends CustomPacketPayload> void sendToPlayer(T packet, Player player) {
        Networking.sendToPlayer(packet, player);
    }

    public static <T extends CustomPacketPayload> void sendToServer(T packet) {
        Networking.sendToServer(packet);
    }
}
