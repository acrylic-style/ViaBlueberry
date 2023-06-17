package net.blueberrymcmods.viablueberry.v1_20.mixin.pipeline.client;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import net.blueberrymcmods.viablueberry.common.handler.BlueberryDecodeHandler;
import net.blueberrymcmods.viablueberry.common.handler.BlueberryEncodeHandler;
import net.blueberrymcmods.viablueberry.common.handler.CommonTransformer;
import net.blueberrymcmods.viablueberry.common.protocol.HostnameParserProtocol;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.network.Connection$1")
public class MixinConnectionChInit {
    @Inject(method = "initChannel", at = @At("TAIL"), remap = false)
    private void onInitChannel(Channel channel, CallbackInfo ci) {
        if (channel instanceof SocketChannel) {
            UserConnection user = new UserConnectionImpl(channel, true);
            new ProtocolPipelineImpl(user).add(HostnameParserProtocol.INSTANCE);

            channel.pipeline()
                    .addBefore("encoder", CommonTransformer.HANDLER_ENCODER_NAME, new BlueberryEncodeHandler(user))
                    .addBefore("decoder", CommonTransformer.HANDLER_DECODER_NAME, new BlueberryDecodeHandler(user));
        }
    }
}
