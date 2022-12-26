package net.blueberrymcmods.viablueberry.v1_19.mixin.gui.client;

import net.blueberrymcmods.viablueberry.common.gui.ViaServerData;
import net.blueberrymcmods.viablueberry.common.handler.BlueberryDecodeHandler;
import net.blueberrymcmods.viablueberry.v1_19.mixin.debug.client.MixinConnectionAccessor;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.client.multiplayer.ServerStatusPinger$1")
public abstract class MixinServerStatusPingerListener implements ClientStatusPacketListener {
    @Shadow
    public abstract @NotNull Connection getConnection();

    @Redirect(method = "handleStatusResponse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ServerData;getIconB64()Ljava/lang/String;"))
    private String onResponseCaptureServerInfo(ServerData serverData) {
        BlueberryDecodeHandler decoder = ((MixinConnectionAccessor) this.getConnection()).getChannel()
                .pipeline().get(BlueberryDecodeHandler.class);
        if (decoder != null) {
            ((ViaServerData) serverData).setViaTranslating(decoder.getInfo().isActive());
            ((ViaServerData) serverData).setViaServerVer(decoder.getInfo().getProtocolInfo().getServerProtocolVersion());
        }
        return serverData.getIconB64();
    }
}
