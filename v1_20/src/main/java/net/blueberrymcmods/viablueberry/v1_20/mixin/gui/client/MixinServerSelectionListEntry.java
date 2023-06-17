package net.blueberrymcmods.viablueberry.v1_20.mixin.gui.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.blueberrymc.common.resources.BlueberryText;
import net.blueberrymcmods.viablueberry.common.gui.ViaServerData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerSelectionList.OnlineServerEntry.class)
public class MixinServerSelectionListEntry {
    private static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");

    @Shadow
    @Final
    private ServerData serverData;

    @Redirect(method = "render", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V"))
    private void redirectPingIcon(GuiGraphics instance, ResourceLocation location, int i, int i2, float f, float f2, int i3, int i4, int i5, int i6) {
        if (location.equals(GUI_ICONS_LOCATION) && ((ViaServerData) this.serverData).isViaTranslating()) {
            RenderSystem.setShaderTexture(i, new ResourceLocation("viablueberry:textures/gui/icons.png"));
            return;
        }
        RenderSystem.setShaderTexture(i, location);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/multiplayer/JoinMultiplayerScreen;setToolTip(Ljava/util/List;)V", ordinal = 0))
    private void addServerVer(JoinMultiplayerScreen screen, List<Component> tooltipText) {
        ProtocolVersion proto = ProtocolVersion.getProtocol(((ViaServerData) this.serverData).getViaServerVer());
        List<Component> lines = new ArrayList<>(tooltipText);
        lines.add(BlueberryText.text("viablueberry", "gui.ping_version.translated", proto.getName()));
        screen.setToolTip(lines);
    }
}
