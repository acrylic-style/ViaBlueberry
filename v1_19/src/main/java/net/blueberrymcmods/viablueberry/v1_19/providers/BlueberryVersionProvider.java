package net.blueberrymcmods.viablueberry.v1_19.providers;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.blueberrymcmods.viablueberry.common.config.VBConfig;
import net.blueberrymcmods.viablueberry.common.providers.AbstractBlueberryVersionProvider;
import net.blueberrymcmods.viablueberry.v1_19.ViaBlueberry;
import net.blueberrymcmods.viablueberry.v1_19.service.ProtocolAutoDetector;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class BlueberryVersionProvider extends AbstractBlueberryVersionProvider {
    @Override
    protected Logger getLogger() {
        return ViaBlueberry.JLOGGER;
    }

    @Override
    protected VBConfig getConfig() {
        return ViaBlueberry.config;
    }

    @Override
    protected CompletableFuture<ProtocolVersion> detectVersion(InetSocketAddress address) {
        return ProtocolAutoDetector.detectVersion(address);
    }
}
