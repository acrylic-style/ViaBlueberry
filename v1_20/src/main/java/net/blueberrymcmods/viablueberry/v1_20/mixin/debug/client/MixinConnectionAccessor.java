package net.blueberrymcmods.viablueberry.v1_20.mixin.debug.client;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Connection.class)
public interface MixinConnectionAccessor {
    @Accessor
    Channel getChannel();
}
