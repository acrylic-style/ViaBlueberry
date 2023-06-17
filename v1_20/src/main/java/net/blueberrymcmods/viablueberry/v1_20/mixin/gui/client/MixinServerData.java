package net.blueberrymcmods.viablueberry.v1_20.mixin.gui.client;

import net.blueberrymcmods.viablueberry.common.gui.ViaServerData;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerData.class)
public class MixinServerData implements ViaServerData {
    private boolean viaTranslating;
    private int viaServerVer;

    @Override
    public int getViaServerVer() {
        return viaServerVer;
    }

    @Override
    public void setViaServerVer(int viaServerVer) {
        this.viaServerVer = viaServerVer;
    }

    @Override
    public boolean isViaTranslating() {
        return viaTranslating;
    }

    @Override
    public void setViaTranslating(boolean via) {
        this.viaTranslating = via;
    }
}
