package net.blueberrymcmods.viablueberry.v1_20.gui;

import net.blueberrymc.common.Side;
import net.blueberrymc.common.SideOnly;
import net.blueberrymc.common.resources.BlueberryText;
import net.blueberrymcmods.viablueberry.common.config.AbstractViaConfigScreen;
import net.blueberrymcmods.viablueberry.common.util.ProtocolUtils;
import net.blueberrymcmods.viablueberry.v1_20.ViaBlueberry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@SideOnly(Side.CLIENT)
public class ViaConfigScreen extends Screen implements AbstractViaConfigScreen {
    private static CompletableFuture<Void> latestProtocolSave;
    private final Screen parent;
    private EditBox protocolVersion;

    public ViaConfigScreen(Screen parent) {
        super(BlueberryText.text("viablueberry", TITLE_TRANSLATE_ID));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int entries = 0;

        this.addRenderableWidget(Button
                .builder(getClientSideText(), this::onClickClientSide)
                .bounds(calculatePosX(this.width, entries), calculatePosY(this.height, entries), 150, 20)
                .build());
        entries++;

        this.addRenderableWidget(Button
                .builder(getHideViaButtonText(), this::onHideViaButton)
                .bounds(calculatePosX(this.width, entries), calculatePosY(this.height, entries), 150, 20)
                .build());
        entries++;

        protocolVersion = new EditBox(font,
                calculatePosX(this.width, entries),
                calculatePosY(this.height, entries),
                150, 20, BlueberryText.text("viablueberry", "gui.protocol_version_field.name"));
        //entries++;

        protocolVersion.setFilter(ProtocolUtils::isStartOfProtocolText);
        protocolVersion.setResponder(this::onChangeVersionField);
        int clientSideVersion = ViaBlueberry.config.getClientSideVersion();
        protocolVersion.setValue(ProtocolUtils.getProtocolName(clientSideVersion));

        this.addRenderableWidget(protocolVersion);

        this.addRenderableWidget(Button
                .builder(CommonComponents.GUI_DONE, it -> onClose())
                .bounds(this.width / 2 - 100, this.height - 40, 200, 20)
                .build());
    }

    private void onChangeVersionField(String text) {
        protocolVersion.setSuggestion(null);
        int newVersion = ViaBlueberry.config.getClientSideVersion();

        Integer parsed = ProtocolUtils.parseProtocolId(text);
        boolean validProtocol;

        if (parsed != null) {
            newVersion = parsed;
            validProtocol = true;
        } else {
            validProtocol = false;
            String[] suggestions = ProtocolUtils.getProtocolSuggestions(text);
            if (suggestions.length == 1) {
                protocolVersion.setSuggestion(suggestions[0].substring(text.length()));
            }
        }

        protocolVersion.setTextColor(getProtocolTextColor(newVersion, validProtocol));

        int finalNewVersion = newVersion;
        if (latestProtocolSave == null) latestProtocolSave = CompletableFuture.completedFuture(null);
        ViaBlueberry.config.setClientSideVersion(finalNewVersion);
        latestProtocolSave = latestProtocolSave.thenRunAsync(ViaBlueberry.config::saveConfig, ViaBlueberry.ASYNC_EXECUTOR);
    }

    private void onClickClientSide(Button widget) {
        if (!ViaBlueberry.config.isClientSideEnabled()) {
            Minecraft.getInstance().setScreen(new ConfirmScreen(
                    answer -> {
                        if (answer) {
                            ViaBlueberry.config.setClientSideEnabled(true);
                            ViaBlueberry.config.setClientSideVersion(-2); // AUTO
                            ViaBlueberry.config.saveConfig();
                            widget.setMessage(getClientSideText());
                        }
                        Minecraft.getInstance().setScreen(this);
                    },
                    BlueberryText.text("viablueberry", "gui.enable_client_side.question"),
                    BlueberryText.text("viablueberry", "gui.enable_client_side.warning"),
                    BlueberryText.text("viablueberry", "gui.enable_client_side.enable"),
                    CommonComponents.GUI_CANCEL
            ));
        } else {
            ViaBlueberry.config.setClientSideEnabled(false);
            ViaBlueberry.config.saveConfig();
        }
        widget.setMessage(getClientSideText());
    }

    @Override
    public void removed() {
        ViaBlueberry.config.saveConfig();
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(this.parent);
    }

    private Component getClientSideText() {
        return ViaBlueberry.config.isClientSideEnabled() ?
                BlueberryText.text("viablueberry", "gui.client_side.disable")
                : BlueberryText.text("viablueberry", "gui.client_side.enable");
    }

    private Component getHideViaButtonText() {
        return ViaBlueberry.config.isHideButton() ?
                BlueberryText.text("viablueberry", "gui.hide_via_button.disable")
                : BlueberryText.text("viablueberry", "gui.hide_via_button.enable");
    }

    private void onHideViaButton(Button widget) {
        ViaBlueberry.config.setHideButton(!ViaBlueberry.config.isHideButton());
        ViaBlueberry.config.saveConfig();
        widget.setMessage(getHideViaButtonText());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaFrameTime) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
        super.render(guiGraphics, mouseX, mouseY, deltaFrameTime);
    }

    @Override
    public void tick() {
        super.tick();
        protocolVersion.tick();
    }
}
