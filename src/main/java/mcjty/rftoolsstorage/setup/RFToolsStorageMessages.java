package mcjty.rftoolsstorage.setup;

import mcjty.lib.network.PacketHandler;
import mcjty.lib.network.PacketRequestDataFromServer;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nonnull;

import static mcjty.lib.network.PlayPayloadContext.wrap;

public class RFToolsStorageMessages {
    public static SimpleChannel INSTANCE;

    public static void registerMessages(String name) {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(RFToolsStorage.MODID, name))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        // Server side
        net.registerMessage(id(), PacketGridToClient.class, PacketGridToClient::write, PacketGridToClient::create, wrap(PacketGridToClient::handle));
        net.registerMessage(id(), PacketSendRecipe.class, PacketSendRecipe::write, PacketSendRecipe::create, wrap(PacketSendRecipe::handle));
        net.registerMessage(id(), PacketCraftTestResultToClient.class, PacketCraftTestResultToClient::write, PacketCraftTestResultToClient::create, wrap(PacketCraftTestResultToClient::handle));
        net.registerMessage(id(), PacketGetInventoryInfo.class, PacketGetInventoryInfo::write, PacketGetInventoryInfo::create, wrap(PacketGetInventoryInfo::handle));
        net.registerMessage(id(), PacketRequestItem.class, PacketRequestItem::write, PacketRequestItem::create, wrap(PacketRequestItem::handle));

        // Client side
        net.registerMessage(id(), PacketStorageInfoToClient.class, PacketStorageInfoToClient::write, PacketStorageInfoToClient::create, wrap(PacketStorageInfoToClient::handle));
        net.registerMessage(id(), PacketGridToServer.class, PacketGridToServer::write, PacketGridToServer::create, wrap(PacketGridToServer::handle));
        net.registerMessage(id(), PacketReturnInventoryInfo.class, PacketReturnInventoryInfo::write, PacketReturnInventoryInfo::create, wrap(PacketReturnInventoryInfo::handle));

        PacketRequestDataFromServer.register(net, id());

        PacketHandler.registerStandardMessages(id(), net);
    }

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void sendToServer(String command, @Nonnull TypedMap.Builder argumentBuilder) {
        INSTANCE.sendToServer(new PacketSendServerCommand(RFToolsStorage.MODID, command, argumentBuilder.build()));
    }

    public static void sendToServer(String command) {
        INSTANCE.sendToServer(new PacketSendServerCommand(RFToolsStorage.MODID, command, TypedMap.EMPTY));
    }

    public static void sendToClient(Player player, String command, @Nonnull TypedMap.Builder argumentBuilder) {
        INSTANCE.sendTo(new PacketSendClientCommand(RFToolsStorage.MODID, command, argumentBuilder.build()), ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToClient(Player player, String command) {
        INSTANCE.sendTo(new PacketSendClientCommand(RFToolsStorage.MODID, command, TypedMap.EMPTY), ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <T> void sendToPlayer(T packet, Player player) {
        INSTANCE.sendTo(packet, ((ServerPlayer)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <T> void sendToServer(T packet) {
        INSTANCE.sendToServer(packet);
    }
}
