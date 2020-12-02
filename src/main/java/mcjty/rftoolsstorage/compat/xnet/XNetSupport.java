package mcjty.rftoolsstorage.compat.xnet;


import mcjty.rftoolsbase.api.xnet.IXNet;

import javax.annotation.Nullable;
import java.util.function.Function;

public class XNetSupport {

    public static IXNet xnet;

    public static class GetXNet implements Function<IXNet, Void> {
        @Nullable
        @Override
        public Void apply(IXNet input) {
            xnet = input;
            xnet.registerChannelType(new StorageChannelType());
            return null;
        }
    }
}
