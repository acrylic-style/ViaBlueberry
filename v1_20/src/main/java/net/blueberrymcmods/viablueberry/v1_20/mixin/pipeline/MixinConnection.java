package net.blueberrymcmods.viablueberry.v1_20.mixin.pipeline;

import io.netty.channel.Channel;
import net.blueberrymcmods.viablueberry.common.handler.PipelineReorderEvent;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class MixinConnection {
    @Shadow
    private Channel channel;

    @Inject(method = "setupCompression", at = @At("RETURN"))
    private void reorderCompression(int compressionThreshold, boolean rejectBad, CallbackInfo ci) {
        channel.pipeline().fireUserEventTriggered(new PipelineReorderEvent());
    }
}
