package net.blueberrymcmods.viablueberry.common.platform;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.libs.fastutil.ints.IntLinkedOpenHashSet;
import com.viaversion.viaversion.libs.fastutil.ints.IntSortedSet;
import com.viaversion.viaversion.libs.fastutil.ints.IntSortedSets;
import com.viaversion.viaversion.libs.gson.JsonObject;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.Side;
import net.blueberrymcmods.viablueberry.common.handler.CommonTransformer;
import net.blueberrymcmods.viablueberry.common.util.NativeVersionProvider;

public class BlueberryInjector implements ViaInjector {
    @Override
    public void inject() {
        // *looks at Mixins*
    }

    @Override
    public void uninject() {
        // not possible *plays sad violin*
    }

    @Override
    public String getEncoderName() {
        return CommonTransformer.HANDLER_ENCODER_NAME;
    }

    @Override
    public String getDecoderName() {
        return CommonTransformer.HANDLER_DECODER_NAME;
    }

    @Override
    public JsonObject getDump() {
        return new JsonObject();
    }

    @Override
    public IntSortedSet getServerProtocolVersions() {
        if (Blueberry.getSide() == Side.CLIENT) {
            int version = Via.getManager().getProviders().get(NativeVersionProvider.class).getNativeServerVersion();
            return IntSortedSets.singleton(version);
        }
        // On client-side we can connect to any server version
        IntSortedSet versions = new IntLinkedOpenHashSet();
        versions.add(ProtocolVersion.v1_7_1.getOriginalVersion());
        versions.add(ProtocolVersion.getProtocols()
                .stream()
                .mapToInt(ProtocolVersion::getOriginalVersion)
                .max().getAsInt());
        return versions;
    }

    @Override
    public int getServerProtocolVersion() {
        return getServerProtocolVersions().firstInt();
    }
}
