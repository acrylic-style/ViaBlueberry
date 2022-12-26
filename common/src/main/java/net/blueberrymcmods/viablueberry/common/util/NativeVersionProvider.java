package net.blueberrymcmods.viablueberry.common.util;

import com.viaversion.viaversion.api.platform.providers.Provider;

public interface NativeVersionProvider extends Provider {
    int getNativeServerVersion();
}
