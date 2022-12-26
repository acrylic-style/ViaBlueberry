package net.blueberrymcmods.viablueberry.common;

import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.configuration.ConfigurationProvider;
import com.viaversion.viaversion.api.configuration.ViaVersionConfig;
import com.viaversion.viaversion.api.platform.UnsupportedSoftware;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonObject;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.Side;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymcmods.viablueberry.common.platform.BlueberryViaAPI;
import net.blueberrymcmods.viablueberry.common.platform.BlueberryViaConfig;
import net.blueberrymcmods.viablueberry.common.util.FutureTaskId;
import net.blueberrymcmods.viablueberry.common.util.JLoggerToLog4j;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public abstract class AbstractBlueberryPlatform implements ViaPlatform<UUID> {
    private final Logger logger = new JLoggerToLog4j(LogManager.getLogger("ViaVersion"));
    private BlueberryViaConfig config;
    private File dataFolder;
    private final ViaAPI<UUID> api;

    {
        api = new BlueberryViaAPI();
    }

    public void init() {
        // We'll use it early for ViaInjector
        installNativeVersionProvider();
        Path configDir = Blueberry.getModLoader().getConfigDir().toPath().resolve("ViaBlueberry");
        dataFolder = configDir.toFile();
        config = new BlueberryViaConfig(configDir.resolve("viaversion.yml").toFile());
    }

    protected abstract void installNativeVersionProvider();

    protected abstract ExecutorService asyncService();

    protected abstract EventLoop eventLoop();

    protected FutureTaskId runEventLoop(Runnable runnable) {
        return new FutureTaskId(eventLoop().submit(runnable).addListener(errorLogger()));
    }

    @Override
    public FutureTaskId runAsync(Runnable runnable) {
        return new FutureTaskId(CompletableFuture.runAsync(runnable, asyncService())
                .exceptionally(throwable -> {
                    if (!(throwable instanceof CancellationException)) {
                        throwable.printStackTrace();
                    }
                    return null;
                }));
    }

    @Override
    public FutureTaskId runSync(Runnable runnable, long ticks) {
        // ViaVersion seems to not need to run delayed tasks on main thread
        return new FutureTaskId(eventLoop()
                .schedule(() -> runSync(runnable), ticks * 50, TimeUnit.MILLISECONDS)
                .addListener(errorLogger())
        );
    }

    @Override
    public FutureTaskId runRepeatingSync(Runnable runnable, long ticks) {
        // ViaVersion seems to not need to run repeating tasks on main thread
        return new FutureTaskId(eventLoop()
                .scheduleAtFixedRate(() -> runSync(runnable), 0, ticks * 50, TimeUnit.MILLISECONDS)
                .addListener(errorLogger())
        );
    }

    protected <T extends Future<?>> GenericFutureListener<T> errorLogger() {
        return future -> {
            if (!future.isCancelled() && future.cause() != null) {
                future.cause().printStackTrace();
            }
        };
    }

    @Override
    public boolean isProxy() {
        // We kinda of have all server versions
        return Blueberry.getSide() == Side.CLIENT;
    }

    @Override
    public void onReload() {
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public ViaVersionConfig getConf() {
        return config;
    }

    @Override
    public ViaAPI<UUID> getApi() {
        return api;
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public String getPluginVersion() {
        return Optional.ofNullable(Blueberry.getModLoader().getModById("viaversion"))
                .map(BlueberryMod::getVersion)
                .orElse("UNKNOWN");
    }

    @Override
    public String getPlatformName() {
        return "ViaBlueberry";
    }

    @Override
    public String getPlatformVersion() {
        return Objects.requireNonNull(Blueberry.getModLoader().getModById("viablueberry")).getVersion();
    }

    @Override
    public boolean isPluginEnabled() {
        return true;
    }

    @Override
    public ConfigurationProvider getConfigurationProvider() {
        return config;
    }

    @Override
    public boolean isOldClientsAllowed() {
        return true;
    }

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
    public final Collection<UnsupportedSoftware> getUnsupportedSoftwareClasses() {
        List<UnsupportedSoftware> list = new ArrayList<>(ViaPlatform.super.getUnsupportedSoftwareClasses());
        return Collections.unmodifiableList(list);
    }

    @Override
    public final boolean hasPlugin(String name) {
        return Blueberry.getModLoader().getModByName(name, true) != null;
    }

    private static final class UnsupportedSoftwareReasons {
        private static final String SELF_INCRIMINATION = "By using these proof-of-concept TESTING mods, " +
                "at best you create fishy context or silly reports, " +
                "at worst you end up incriminating yourself when writing messages or reporting another player.";
    }
}
