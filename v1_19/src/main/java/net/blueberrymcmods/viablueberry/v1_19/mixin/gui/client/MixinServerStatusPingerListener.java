package net.blueberrymcmods.viablueberry.v1_19.mixin.gui.client;

import net.blueberrymcmods.viablueberry.common.gui.ViaServerData;
import net.blueberrymcmods.viablueberry.common.handler.BlueberryDecodeHandler;
import net.blueberrymcmods.viablueberry.v1_19.mixin.debug.client.MixinConnectionAccessor;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.multiplayer.ServerStatusPinger$1")
public abstract class MixinServerStatusPingerListener implements ClientStatusPacketListener {
    @Shadow @Final ServerData val$serverData;
    @Shadow @Final Connection val$connection;

    @Inject(method = "handleStatusResponse", at = @At("HEAD"))
    private void onResponseCaptureServerInfo(CallbackInfo ci) {
        BlueberryDecodeHandler decoder = ((MixinConnectionAccessor) val$connection).getChannel()
                .pipeline().get(BlueberryDecodeHandler.class);
        if (decoder != null) {
            ((ViaServerData) val$serverData).setViaTranslating(decoder.getInfo().isActive());
            ((ViaServerData) val$serverData).setViaServerVer(decoder.getInfo().getProtocolInfo().getServerProtocolVersion());
        }
    }
}
