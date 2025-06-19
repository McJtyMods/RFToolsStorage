package mcjty.rftoolsstorage.compat.xnet;


import mcjty.rftoolsbase.api.xnet.IXNet;

import javax.annotation.Nullable;
import java.util.function.Function;

public class XNetSupport {

    public static IXNet xnet;
    public static StorageChannelType storageChannelType;

    public static class GetXNet implements Function<IXNet, Void> {
        @Nullable
        @Override
        public Void apply(IXNet input) {
            xnet = input;
            storageChannelType = new StorageChannelType();
            xnet.registerChannelType(storageChannelType);
            return null;
        }
    }
}
