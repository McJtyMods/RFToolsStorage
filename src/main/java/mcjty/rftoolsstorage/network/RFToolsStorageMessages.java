package mcjty.rftoolsstorage.network;

import mcjty.lib.network.*;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import javax.annotation.Nonnull;

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
        PacketHandler.debugRegister("RFToolsStorage", net, id(), PacketRequestStorageFromServer.class, PacketRequestStorageFromServer::toBytes, PacketRequestStorageFromServer::new, PacketRequestStorageFromServer::handle);
        PacketHandler.debugRegister("RFToolsStorage", net, id(), PacketGridToClient.class, PacketGridToClient::toBytes, PacketGridToClient::new, PacketGridToClient::handle);
        PacketHandler.debugRegister("RFToolsStorage", net, id(), PacketSendRecipe.class, PacketSendRecipe::toBytes, PacketSendRecipe::new, PacketSendRecipe::handle);
        PacketHandler.debugRegister("RFToolsStorage", net, id(), PacketCraftTestResultToClient.class, PacketCraftTestResultToClient::toBytes, PacketCraftTestResultToClient::new, PacketCraftTestResultToClient::handle);
        PacketHandler.debugRegister("RFToolsStorage", net, id(), PacketGetInventoryInfo.class, PacketGetInventoryInfo::toBytes, PacketGetInventoryInfo::new, PacketGetInventoryInfo::handle);
        PacketHandler.debugRegister("RFToolsStorage", net, id(), PacketRequestItem.class, PacketRequestItem::toBytes, PacketRequestItem::new, PacketRequestItem::handle);

        // Client side
        PacketHandler.debugRegister("RFToolsStorage", net, id(), PacketReturnStorageToClient.class, PacketReturnStorageToClient::toBytes, PacketReturnStorageToClient::new, PacketReturnStorageToClient::handle);
        PacketHandler.debugRegister("RFToolsStorage", net, id(), PacketStorageInfoToClient.class, PacketStorageInfoToClient::toBytes, PacketStorageInfoToClient::new, PacketStorageInfoToClient::handle);
        PacketHandler.debugRegister("RFToolsStorage", net, id(), PacketGridToServer.class, PacketGridToServer::toBytes, PacketGridToServer::new, PacketGridToServer::handle);
        PacketHandler.debugRegister("RFToolsStorage", net, id(), PacketReturnInventoryInfo.class, PacketReturnInventoryInfo::toBytes, PacketReturnInventoryInfo::new, PacketReturnInventoryInfo::handle);

        PacketHandler.debugRegister("RFToolsStorage", net, id(), PacketRequestDataFromServer.class, PacketRequestDataFromServer::toBytes, PacketRequestDataFromServer::new,
                new ChannelBoundHandler<>(net, PacketRequestDataFromServer::handle));

        PacketHandler.registerStandardMessages("RFToolsStorage - Standard", id(), net);
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

    public static void sendToClient(PlayerEntity player, String command, @Nonnull TypedMap.Builder argumentBuilder) {
        INSTANCE.sendTo(new PacketSendClientCommand(RFToolsStorage.MODID, command, argumentBuilder.build()), ((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToClient(PlayerEntity player, String command) {
        INSTANCE.sendTo(new PacketSendClientCommand(RFToolsStorage.MODID, command, TypedMap.EMPTY), ((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }
}
