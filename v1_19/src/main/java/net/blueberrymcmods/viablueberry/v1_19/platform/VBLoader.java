package net.blueberrymcmods.viablueberry.v1_19.platform;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.platform.ViaPlatformLoader;
import com.viaversion.viaversion.api.protocol.version.VersionProvider;
import com.viaversion.viaversion.bungee.providers.BungeeMovementTransmitter;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.HandItemProvider;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.Side;
import net.blueberrymcmods.viablueberry.v1_19.providers.BlueberryVersionProvider;
import net.blueberrymcmods.viablueberry.v1_19.providers.VRHandItemProvider;

public class VBLoader implements ViaPlatformLoader {
    @Override
    public void load() {
        Via.getManager().getProviders().use(MovementTransmitterProvider.class, new BungeeMovementTransmitter());
        Via.getManager().getProviders().use(VersionProvider.class, new BlueberryVersionProvider());

        if (Via.getPlatform().getConf().isItemCache()) {
            VRHandItemProvider handProvider = new VRHandItemProvider();
            if (Blueberry.getSide() == Side.CLIENT) {
                handProvider.registerClientTick();
            }
            Via.getManager().getProviders().use(HandItemProvider.class, handProvider);
        }
    }

    @Override
    public void unload() {
        // nothing to do
    }
}
