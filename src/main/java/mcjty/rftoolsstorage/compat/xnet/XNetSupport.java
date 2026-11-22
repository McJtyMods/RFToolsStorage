package mcjty.rftoolsstorage.compat.xnet;

import mcjty.rftoolsbase.api.xnet.IXNet;
import mcjty.rftoolsbase.api.xnet.XNetPlugin;

public class XNetSupport implements XNetPlugin {

    public static IXNet xnet;
    public static StorageChannelType storageChannelType;

    @Override
    public void register(IXNet api) {
        xnet = api;
        storageChannelType = new StorageChannelType();
        api.registerChannelType(storageChannelType);
    }
}
