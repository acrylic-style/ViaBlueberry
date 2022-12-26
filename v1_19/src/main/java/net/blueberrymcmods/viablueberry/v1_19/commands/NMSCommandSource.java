package net.blueberrymcmods.viablueberry.v1_19.commands;

import com.viaversion.viaversion.api.command.ViaCommandSender;
import net.blueberrymcmods.viablueberry.common.util.RemappingUtil;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public class NMSCommandSource implements ViaCommandSender {
    private final CommandSource source;

    public NMSCommandSource(CommandSource source) {
        this.source = source;
    }

    @Override
    public boolean hasPermission(String permission) {
        if (source.getClass().getTypeName().equals("net.minecraft.client.player.LocalPlayer")) {
            return true;
        }
        if (source instanceof CommandSourceStack commandSourceStack) {
            return commandSourceStack.hasPermission(3);
        } else {
            return source.hasPermission(permission);
        }
    }

    @Override
    public void sendMessage(String msg) {
        if (source instanceof CommandSourceStack commandSourceStack) {
            commandSourceStack.sendSuccess(fromLegacy(msg), false);
        } else {
            source.sendSystemMessage(fromLegacy(msg));
        }
    }

    public static MutableComponent fromLegacy(String legacy) {
        return Component.Serializer.fromJson(RemappingUtil.legacyToJson(legacy));
    }

    @Override
    public UUID getUUID() {
        if (source instanceof Entity entity) {
            return entity.getUUID();
        } else if (source instanceof CommandSourceStack commandSourceStack) {
            Entity entity = commandSourceStack.getEntity();
            if (entity != null) return entity.getUUID();
        }
        return UUID.fromString(getName());
    }

    @Override
    public String getName() {
        if (source instanceof Entity entity) {
            return entity.getScoreboardName();
        } else if (source instanceof CommandSourceStack commandSourceStack) {
            return commandSourceStack.getTextName();
        }
        return "?";
    }
}
