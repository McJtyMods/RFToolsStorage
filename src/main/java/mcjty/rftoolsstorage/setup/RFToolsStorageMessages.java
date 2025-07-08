package mcjty.rftoolsstorage.setup;

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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import javax.annotation.Nonnull;

public class RFToolsStorageMessages {

    public static void registerMessages(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(RFToolsStorage.MODID)
                .versioned("1.0")
                .optional();

        // Server side
        registrar.playToServer(PacketGridToServer.TYPE, PacketGridToServer.CODEC, PacketGridToServer::handle);
        registrar.playToServer(PacketSendRecipe.TYPE, PacketSendRecipe.CODEC, PacketSendRecipe::handle);
        registrar.playToServer(PacketCraftTestResultToClient.TYPE, PacketCraftTestResultToClient.CODEC, PacketCraftTestResultToClient::handle);
        registrar.playToServer(PacketGetInventoryInfo.TYPE, PacketGetInventoryInfo.CODEC, PacketGetInventoryInfo::handle);
        registrar.playToServer(PacketRequestItem.TYPE, PacketRequestItem.CODEC, PacketRequestItem::handle);

        // Client side
        registrar.playToClient(PacketGridToClient.TYPE, PacketGridToClient.CODEC, PacketGridToClient::handle);
        registrar.playToClient(PacketStorageInfoToClient.TYPE, PacketStorageInfoToClient.CODEC, PacketStorageInfoToClient::handle);
        registrar.playToClient(PacketReturnInventoryInfo.TYPE, PacketReturnInventoryInfo.CODEC, PacketReturnInventoryInfo::handle);
    }

    public static void sendToServer(String command, @Nonnull TypedMap.Builder argumentBuilder) {
        Networking.sendToServer(new PacketSendServerCommand(RFToolsStorage.MODID, command, argumentBuilder.build()));
    }

    public static void sendToClient(Player player, String command, @Nonnull TypedMap.Builder argumentBuilder) {
        Networking.sendToPlayer(new PacketSendClientCommand(RFToolsStorage.MODID, command, argumentBuilder.build()), player);
    }

    public static <T extends CustomPacketPayload> void sendToPlayer(T packet, Player player) {
        PacketDistributor.sendToPlayer((ServerPlayer)player, packet);
    }

    public static <T extends CustomPacketPayload> void sendToServer(T packet) {
        PacketDistributor.sendToServer(packet);
    }
}
