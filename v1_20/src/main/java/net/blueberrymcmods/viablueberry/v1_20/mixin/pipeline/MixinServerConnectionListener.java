package net.blueberrymcmods.viablueberry.v1_20.mixin.pipeline;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import net.blueberrymcmods.viablueberry.common.handler.BlueberryDecodeHandler;
import net.blueberrymcmods.viablueberry.common.handler.BlueberryEncodeHandler;
import net.blueberrymcmods.viablueberry.common.handler.CommonTransformer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.network.ServerConnectionListener$1")
public class MixinServerConnectionListener {
    @Inject(method = "initChannel", at = @At("TAIL"), remap = false)
    private void onInitChannel(Channel channel, CallbackInfo ci) {
        if (channel instanceof SocketChannel) {
            UserConnection user = new UserConnectionImpl(channel);
            new ProtocolPipelineImpl(user);

            channel.pipeline().addBefore("encoder", CommonTransformer.HANDLER_ENCODER_NAME, new BlueberryEncodeHandler(user));
            channel.pipeline().addBefore("decoder", CommonTransformer.HANDLER_DECODER_NAME, new BlueberryDecodeHandler(user));
        }
    }
}
