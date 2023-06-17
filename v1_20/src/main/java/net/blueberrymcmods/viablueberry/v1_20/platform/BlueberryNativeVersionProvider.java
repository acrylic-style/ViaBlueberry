package net.blueberrymcmods.viablueberry.v1_20.platform;

import net.blueberrymcmods.viablueberry.common.util.NativeVersionProvider;
import net.minecraft.SharedConstants;

public class BlueberryNativeVersionProvider implements NativeVersionProvider {
    @Override
    public int getNativeServerVersion() {
        return SharedConstants.getCurrentVersion().getProtocolVersion();
    }
}
