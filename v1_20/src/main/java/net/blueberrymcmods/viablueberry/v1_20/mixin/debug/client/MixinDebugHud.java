package net.blueberrymcmods.viablueberry.v1_20.mixin.debug.client;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.netty.channel.ChannelHandler;
import net.blueberrymcmods.viablueberry.common.handler.BlueberryDecodeHandler;
import net.blueberrymcmods.viablueberry.common.handler.CommonTransformer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class MixinDebugHud {
    @Inject(method = "getGameInformation", at = @At("RETURN"))
    protected void getGameInformation(CallbackInfoReturnable<List<String>> info) {
        String line = "[ViaBlueberry] I: " + Via.getManager().getConnectionManager().getConnections().size() + " (F: "
                + Via.getManager().getConnectionManager().getConnectedClients().size() + ")";
        @SuppressWarnings("ConstantConditions") ChannelHandler viaDecoder = ((MixinConnectionAccessor) Minecraft.getInstance().getConnection()
                .getConnection()).getChannel().pipeline().get(CommonTransformer.HANDLER_DECODER_NAME);
        if (viaDecoder instanceof BlueberryDecodeHandler) {
            ProtocolInfo protocol = ((BlueberryDecodeHandler) viaDecoder).getInfo().getProtocolInfo();
            if (protocol != null) {
                ProtocolVersion serverVer = ProtocolVersion.getProtocol(protocol.getServerProtocolVersion());
                ProtocolVersion clientVer = ProtocolVersion.getProtocol(protocol.getProtocolVersion());
                line += " / C: " + clientVer + " S: " + serverVer;
            }
        }
        info.getReturnValue().add(line);
    }
}
