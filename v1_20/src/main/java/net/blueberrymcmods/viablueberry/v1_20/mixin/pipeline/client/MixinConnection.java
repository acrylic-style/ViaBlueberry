package net.blueberrymcmods.viablueberry.v1_20.mixin.pipeline.client;

import net.blueberrymcmods.viablueberry.v1_20.ViaBlueberry;
import net.blueberrymcmods.viablueberry.v1_20.service.ProtocolAutoDetector;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Mixin(Connection.class)
public class MixinConnection {
    @Inject(method = "connectToServer", at = @At("HEAD"))
    private static void onConnect(InetSocketAddress address, boolean useEpoll, CallbackInfoReturnable<Connection> cir) {
        try {
            if (!ViaBlueberry.config.isClientSideEnabled()) return;
            ProtocolAutoDetector.detectVersion(address).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            ViaBlueberry.JLOGGER.log(Level.WARNING, "Could not auto-detect protocol for " + address + " " + e);
        }
    }
}
