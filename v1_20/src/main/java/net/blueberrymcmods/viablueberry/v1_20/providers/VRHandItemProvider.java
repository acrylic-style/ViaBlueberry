package net.blueberrymcmods.viablueberry.v1_20.providers;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.HandItemProvider;
import net.blueberrymc.client.event.gameevent.ClientTickEvent;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.EventPriority;
import net.blueberrymc.common.util.VoidSafeExecutor;
import net.blueberrymcmods.viablueberry.common.util.RemappingUtil;
import net.blueberrymcmods.viablueberry.v1_20.ViaBlueberry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class VRHandItemProvider extends HandItemProvider {
    public Item clientItem = null;

    @Override
    public Item getHandItem(UserConnection info) {
        if (info.isClientSide()) {
            return getClientItem();
        }
        return super.getHandItem(info);
    }

    private Item getClientItem() {
        if (clientItem == null) {
            return new DataItem(0, (byte) 0, (short) 0, null);
        }
        return new DataItem(clientItem);
    }

    public void registerClientTick() {
        Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
            @Override
            public void execute() {
                Blueberry.getEventManager()
                        .registerEvent(ClientTickEvent.class, ViaBlueberry.instance, EventPriority.NORMAL, event -> tickClient());
            }
        });
    }

    private void tickClient() {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p != null) {
            clientItem = fromNative(p.getInventory().getSelected());
        }
    }

    private Item fromNative(ItemStack original) {
        ResourceLocation location = BuiltInRegistries.ITEM.getKey(original.getItem());
        int id = RemappingUtil.swordId(location.toString());
        return new DataItem(id, (byte) original.getCount(), (short) original.getDamageValue(), null);
    }
}
