package net.blueberrymcmods.viablueberry.v1_19.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.blueberrymc.common.Side;
import net.blueberrymc.common.SideOnly;
import net.blueberrymc.network.client.handshake.ClientboundBlueberryHandshakePacket;
import net.blueberrymcmods.viablueberry.common.AddressParser;
import net.blueberrymcmods.viablueberry.v1_19.ViaBlueberry;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.Varint21FrameDecoder;
import net.minecraft.network.Varint21LengthFieldPrepender;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import net.minecraft.network.protocol.status.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@SideOnly(Side.CLIENT)
public class ProtocolAutoDetector {
    private static final LoadingCache<InetSocketAddress, CompletableFuture<ProtocolVersion>> SERVER_VER = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build(CacheLoader.from((address) -> {
                CompletableFuture<ProtocolVersion> future = new CompletableFuture<>();

                try {
                    final Connection clientConnection = new Connection(PacketFlow.CLIENTBOUND);

                    ChannelFuture ch = new Bootstrap()
                            .group(Connection.NETWORK_WORKER_GROUP.get())
                            .channel(NioSocketChannel.class)
                            .handler(new ChannelInitializer<Channel>() {
                                protected void initChannel(Channel channel) {
                                    try {
                                        channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                                        channel.config().setOption(ChannelOption.IP_TOS, 0x18); // Stolen from Velocity, low delay, high reliability
                                    } catch (ChannelException ignored) {
                                    }

                                    channel.pipeline()
                                            .addLast("timeout", new ReadTimeoutHandler(30))
                                            .addLast("splitter", new Varint21FrameDecoder())
                                            .addLast("decoder", new PacketDecoder(PacketFlow.CLIENTBOUND))
                                            .addLast("prepender", new Varint21LengthFieldPrepender())
                                            .addLast("encoder", new PacketEncoder(PacketFlow.SERVERBOUND))
                                            .addLast("packet_handler", clientConnection);
                                }
                            })
                            .connect(address);

                    ch.addListener(future1 -> {
                        if (!future1.isSuccess()) {
                            future.completeExceptionally(future1.cause());
                        } else {
                            ch.channel().eventLoop().execute(() -> { // needs to execute after channel init
                                clientConnection.setListener(new ClientStatusPacketListener() {
                                    @Override
                                    public void handleBlueberryHandshakeResponse(@NotNull ClientboundBlueberryHandshakePacket clientboundBlueberryHandshakePacket) {

                                    }

                                    @Override
                                    public void handleStatusResponse(ClientboundStatusResponsePacket packet) {
                                        ServerStatus meta = packet.getStatus();
                                        ServerStatus.Version version;
                                        if (meta != null && (version = meta.getVersion()) != null) {
                                            ProtocolVersion ver = ProtocolVersion.getProtocol(version.getProtocol());
                                            future.complete(ver);
                                            ViaBlueberry.JLOGGER.info("Auto-detected " + ver + " for " + address);
                                        } else {
                                            future.completeExceptionally(new IllegalArgumentException("Null version in query response"));
                                        }
                                        clientConnection.disconnect(Component.empty());
                                    }

                                    @Override
                                    public void handlePongResponse(ClientboundPongResponsePacket packet) {
                                        clientConnection.disconnect(Component.literal("Pong not requested!"));
                                    }

                                    @Override
                                    public void onDisconnect(Component reason) {
                                        future.completeExceptionally(new IllegalStateException(reason.getString()));
                                    }

                                    @Override
                                    public Connection getConnection() {
                                        return clientConnection;
                                    }
                                });

                                clientConnection.send(new ClientIntentionPacket(address.getHostString(),
                                        address.getPort(), ConnectionProtocol.STATUS));
                                clientConnection.send(new ServerboundStatusRequestPacket());
                            });
                        }
                    });
                } catch (Throwable throwable) {
                    future.completeExceptionally(throwable);
                }

                return future;
            }));

    public static CompletableFuture<ProtocolVersion> detectVersion(InetSocketAddress address) {
        try {
            InetSocketAddress real = new InetSocketAddress(InetAddress.getByAddress
                    (new AddressParser().parse(address.getHostString()).serverAddress,
                            address.getAddress().getAddress()), address.getPort());
            return SERVER_VER.get(real);
        } catch (UnknownHostException | ExecutionException e) {
            ViaBlueberry.JLOGGER.log(Level.WARNING, "Protocol auto detector error: ", e);
            return CompletableFuture.completedFuture(null);
        }
    }
}
