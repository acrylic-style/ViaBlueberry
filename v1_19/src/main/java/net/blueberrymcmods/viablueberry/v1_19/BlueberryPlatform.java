package net.blueberrymcmods.viablueberry.v1_19;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonObject;
import io.netty.channel.EventLoop;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.Side;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymcmods.viablueberry.common.AbstractBlueberryPlatform;
import net.blueberrymcmods.viablueberry.common.commands.UserCommandSender;
import net.blueberrymcmods.viablueberry.common.util.FutureTaskId;
import net.blueberrymcmods.viablueberry.common.util.NativeVersionProvider;
import net.blueberrymcmods.viablueberry.v1_19.platform.BlueberryNativeVersionProvider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class BlueberryPlatform extends AbstractBlueberryPlatform {
    @Override
    public JsonObject getDump() {
        JsonObject platformSpecific = new JsonObject();
        JsonArray mods = new JsonArray();
        Blueberry.getModLoader().getLoadedMods().stream().map((mod) -> {
            JsonObject jsonMod = new JsonObject();
            jsonMod.addProperty("id", mod.getModId());
            jsonMod.addProperty("name", mod.getName());
            jsonMod.addProperty("version", mod.getVersion());
            JsonArray authors = new JsonArray();
            List<String> authorsList = mod.getDescription().getAuthors();
            if (authorsList != null) {
                authorsList.forEach(authors::add);
            }
            jsonMod.add("authors", authors);

            return jsonMod;
        }).forEach(mods::add);

        platformSpecific.add("mods", mods);
        return platformSpecific;
    }

    @Override
    public boolean isProxy() {
        // We kinda of have all server versions
        return Blueberry.getSide() == Side.CLIENT;
    }

    @Override
    public String getPluginVersion() {
        return Optional.ofNullable(Blueberry.getModLoader().getModById("viaversion"))
                .map(BlueberryMod::getVersion)
                .orElse("UNKNOWN");
    }

    @Override
    public FutureTaskId runSync(Runnable runnable) {
        return new FutureTaskId(CompletableFuture.runAsync(runnable, r -> Blueberry.getUtil().getClientSchedulerOptional().orElse(Blueberry.getUtil().getServerScheduler()).runTask(ViaBlueberry.instance, r)));
    }

    @Override
    public ViaCommandSender[] getOnlinePlayers() {
        return Via.getManager().getConnectionManager().getConnectedClients().values().stream()
                .map(UserCommandSender::new)
                .toArray(ViaCommandSender[]::new);
    }

    @Override
    public void sendMessage(UUID uuid, String message) {

    }

    @Override
    public boolean kickPlayer(UUID uuid, String message) {
        return false;
    }

    @Override
    public String getPlatformVersion() {
        return Blueberry.getVersion().getFullyQualifiedVersion();
    }

    @Override
    protected void installNativeVersionProvider() {
        Via.getManager().getProviders().use(NativeVersionProvider.class, new BlueberryNativeVersionProvider());
    }

    @Override
    protected ExecutorService asyncService() {
        return ViaBlueberry.ASYNC_EXECUTOR;
    }

    @Override
    protected EventLoop eventLoop() {
        return ViaBlueberry.EVENT_LOOP;
    }
}
