package net.blueberrymcmods.viablueberry.v1_19.mixin.gui.client;

import net.blueberrymc.common.resources.BlueberryText;
import net.blueberrymcmods.viablueberry.v1_19.ViaBlueberry;
import net.blueberrymcmods.viablueberry.v1_19.gui.ViaConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public abstract class MixinJoinMultiplayerScreen extends Screen {
    // dummy constructor
    protected MixinJoinMultiplayerScreen(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        Button button = new ImageButton(width / 2 + 113, 10,
                40, 20, // size
                0, 0, // start poos of texture
                20, // v hover offset
                new ResourceLocation("viablueberry:textures/gui/widgets.png"),
                256, 256, // texture size
                it -> Minecraft.getInstance().setScreen(new ViaConfigScreen(this)),
                BlueberryText.text("blueberry", "gui.via_button")
        );
        if (ViaBlueberry.config.isHideButton()) button.visible = false;
        this.addRenderableWidget(button);
    }
}
