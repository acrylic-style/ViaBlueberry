package net.blueberrymcmods.viablueberry.common.providers;

import com.google.common.primitives.Ints;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.protocols.base.BaseVersionProvider;
import net.blueberrymcmods.viablueberry.common.AddressParser;
import net.blueberrymcmods.viablueberry.common.config.VBConfig;
import net.blueberrymcmods.viablueberry.common.util.NativeVersionProvider;
import net.blueberrymcmods.viablueberry.common.util.ProtocolUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public abstract class AbstractBlueberryVersionProvider extends BaseVersionProvider {
    @Override
    public int getClosestServerProtocol(UserConnection connection) throws Exception {
        if (connection.isClientSide()) {
            ProtocolInfo info = Objects.requireNonNull(connection.getProtocolInfo());

            if (!getConfig().isClientSideEnabled()) return info.getProtocolVersion();

            int serverVer = getConfig().getClientSideVersion();
            SocketAddress addr = connection.getChannel().remoteAddress();

            if (addr instanceof InetSocketAddress) {
                AddressParser parser = new AddressParser();
                Integer addrVersion = parser.parse(((InetSocketAddress) addr).getHostName()).protocol;
                if (addrVersion != null) {
                    serverVer = addrVersion;
                }

                try {
                    if (serverVer == -2) {
                        // Hope protocol was autodetected
                        ProtocolVersion autoVer =
                                detectVersion((InetSocketAddress) addr).getNow(null);
                        if (autoVer != null) {
                            serverVer = autoVer.getVersion();
                        }
                    }
                } catch (Exception e) {
                    getLogger().warning("Couldn't auto detect: " + e);
                }
            }

            boolean blocked = checkAddressBlocked(addr);
            boolean supported = ProtocolUtils.isSupported(serverVer, info.getProtocolVersion());

            if (blocked || !supported) serverVer = info.getProtocolVersion();

            return serverVer;
        }
        NativeVersionProvider natProvider = Via.getManager().getProviders().get(NativeVersionProvider.class);
        if (natProvider != null) {
            return ProtocolVersion.getProtocol(natProvider.getNativeServerVersion()).getVersion();
        }
        return super.getClosestServerProtocol(connection);
    }

    private boolean checkAddressBlocked(SocketAddress addr) {
        return addr instanceof InetSocketAddress && (isDisabled(((InetSocketAddress) addr).getHostString())
                || ((((InetSocketAddress) addr).getAddress() != null) &&
                (isDisabled(((InetSocketAddress) addr).getAddress().getHostAddress())
                        || isDisabled(((InetSocketAddress) addr).getAddress().getHostName()))));
    }

    private boolean isDisabled(String addr) {
        String[] parts = addr.split("\\.");
        boolean isNumericIp = parts.length == 4 && Arrays.stream(parts).map(Ints::tryParse).allMatch(Objects::nonNull);
        return IntStream.range(0, parts.length).anyMatch(i -> {
            String query;
            if (isNumericIp) {
                query = String.join(".", Arrays.stream(parts, 0, i + 1)
                        .toArray(String[]::new)) + ((i != 3) ? ".*" : "");
            } else {
                query = ((i != 0) ? "*." : "") + String.join(".", Arrays.stream(parts, i, parts.length)
                        .toArray(String[]::new));
            }
            if (getConfig().isForcedDisable(query)) {
                getLogger().info(addr + " is force-disabled. (Matches " + query + ")");
                return true;
            } else {
                return false;
            }
        });
    }

    protected abstract Logger getLogger();

    protected abstract VBConfig getConfig();

    protected abstract CompletableFuture<ProtocolVersion> detectVersion(InetSocketAddress address);
}
