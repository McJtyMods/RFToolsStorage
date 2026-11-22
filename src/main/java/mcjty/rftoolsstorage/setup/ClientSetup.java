package mcjty.rftoolsstorage.setup;

import mcjty.lib.network.PayloadContext;
import mcjty.rftoolsstorage.RFToolsStorage;
import mcjty.rftoolsstorage.craftinggrid.PacketCraftTestResultToClient;
import mcjty.rftoolsstorage.craftinggrid.PacketGridToClient;
import mcjty.rftoolsstorage.modules.craftingmanager.client.GuiCraftingManager;
import mcjty.rftoolsstorage.modules.modularstorage.client.GuiModularStorage;
import mcjty.rftoolsstorage.modules.modularstorage.network.PacketStorageInfoToClient;
import mcjty.rftoolsstorage.modules.scanner.client.GuiStorageScanner;
import mcjty.rftoolsstorage.modules.scanner.network.PacketReturnInventoryInfo;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ClientSetup implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        GuiCraftingManager.register();
        GuiModularStorage.register();
        GuiStorageScanner.register();
        RFToolsStorage.modules.initClient();
        ClientPlayNetworking.registerGlobalReceiver(PacketGridToClient.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.client())));
        ClientPlayNetworking.registerGlobalReceiver(PacketStorageInfoToClient.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.client())));
        ClientPlayNetworking.registerGlobalReceiver(PacketReturnInventoryInfo.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.client())));
        ClientPlayNetworking.registerGlobalReceiver(PacketCraftTestResultToClient.TYPE, (packet, context) ->
                packet.handle(new PayloadContext(context.player(), context.client())));
    }
}
