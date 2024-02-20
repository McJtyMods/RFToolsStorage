package mcjty.rftoolsstorage.setup;

import mcjty.lib.network.IPayloadRegistrar;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;

import javax.annotation.Nonnull;

public class RFToolsStorageMessages {

    private static IPayloadRegistrar registrar;

    public static void registerMessages() {
        registrar = Networking.registrar(RFToolsStorage.MODID)
                .versioned("1.0")
                .optional();

        // Server side
        registrar.play(PacketGridToClient.class, PacketGridToClient::create, handler -> handler.server(PacketGridToClient::handle));
        registrar.play(PacketSendRecipe.class, PacketSendRecipe::create, handler -> handler.server(PacketSendRecipe::handle));
        registrar.play(PacketCraftTestResultToClient.class, PacketCraftTestResultToClient::create, handler -> handler.server(PacketCraftTestResultToClient::handle));
        registrar.play(PacketGetInventoryInfo.class, PacketGetInventoryInfo::create, handler -> handler.server(PacketGetInventoryInfo::handle));
        registrar.play(PacketRequestItem.class, PacketRequestItem::create, handler -> handler.server(PacketRequestItem::handle));

        // Client side
        registrar.play(PacketStorageInfoToClient.class, PacketStorageInfoToClient::create, handler -> handler.client(PacketStorageInfoToClient::handle));
        registrar.play(PacketGridToServer.class, PacketGridToServer::create, handler -> handler.client(PacketGridToServer::handle));
        registrar.play(PacketReturnInventoryInfo.class, PacketReturnInventoryInfo::create, handler -> handler.client(PacketReturnInventoryInfo::handle));
    }

    public static void sendToServer(String command, @Nonnull TypedMap.Builder argumentBuilder) {
        Networking.sendToServer(new PacketSendServerCommand(RFToolsStorage.MODID, command, argumentBuilder.build()));
    }

    public static void sendToClient(Player player, String command, @Nonnull TypedMap.Builder argumentBuilder) {
        Networking.sendToPlayer(new PacketSendClientCommand(RFToolsStorage.MODID, command, argumentBuilder.build()), player);
    }

    public static <T> void sendToPlayer(T packet, Player player) {
        registrar.getChannel().sendTo(packet, ((ServerPlayer)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <T> void sendToServer(T packet) {
        registrar.getChannel().sendToServer(packet);
    }
}
