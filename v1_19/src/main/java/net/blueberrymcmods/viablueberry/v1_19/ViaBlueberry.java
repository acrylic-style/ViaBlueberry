package net.blueberrymcmods.viablueberry.v1_19;

import com.google.common.collect.Range;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoop;
import net.blueberrymc.client.commands.ClientCommandManager;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.bml.event.EventHandler;
import net.blueberrymc.common.event.command.CommandRegistrationEvent;
import net.blueberrymc.common.util.VoidSafeExecutor;
import net.blueberrymcmods.viablueberry.common.config.VBConfig;
import net.blueberrymcmods.viablueberry.common.platform.BlueberryInjector;
import net.blueberrymcmods.viablueberry.common.protocol.HostnameParserProtocol;
import net.blueberrymcmods.viablueberry.common.util.JLoggerToLog4j;
import net.blueberrymcmods.viablueberry.v1_19.commands.VRCommandHandler;
import net.blueberrymcmods.viablueberry.v1_19.platform.VBLoader;
import net.minecraft.commands.CommandSourceStack;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

public class ViaBlueberry extends BlueberryMod {
    public static final Logger JLOGGER = new JLoggerToLog4j(LogManager.getLogger("ViaBlueberry"));
    public static final ExecutorService ASYNC_EXECUTOR;
    public static final EventLoop EVENT_LOOP;
    public static final CompletableFuture<Void> INIT_FUTURE = new CompletableFuture<>();
    public static VBConfig config;
    public static ViaBlueberry instance;

    static {
        ThreadFactory factory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ViaBlueberry-%d").build();
        ASYNC_EXECUTOR = Executors.newFixedThreadPool(8, factory);
        EVENT_LOOP = new DefaultEventLoop(factory);
        EVENT_LOOP.submit(INIT_FUTURE::join); // https://github.com/ViaVersion/ViaFabric/issues/53 ugly workaround code but works tm
    }

    public ViaBlueberry() {
        instance = this;
    }

    public static <S extends CommandSourceStack> @NotNull LiteralArgumentBuilder<S> command(@NotNull String commandName) {
        return LiteralArgumentBuilder.<S>literal(commandName)
                .then(
                        RequiredArgumentBuilder
                                .<S, String>argument("args", StringArgumentType.greedyString())
                                .executes(((VRCommandHandler) Via.getManager().getCommandHandler())::execute)
                                .suggests(((VRCommandHandler) Via.getManager().getCommandHandler())::suggestion)
                )
                .executes(((VRCommandHandler) Via.getManager().getCommandHandler())::execute);
    }

    @Override
    public void onLoad() {
        Blueberry.getEventManager().registerEvents(this, this);

        BlueberryPlatform platform = new BlueberryPlatform();

        Via.init(ViaManagerImpl.builder()
                .injector(new BlueberryInjector())
                .loader(new VBLoader())
                .commandHandler(new VRCommandHandler())
                .platform(platform).build());

        platform.init();

        if (Blueberry.getModLoader().getModById("viabackwards") != null) {
            MappingDataLoader.enableMappingsCache();
        }

        ((ViaManagerImpl) Via.getManager()).init();

        Via.getManager().getProtocolManager().registerBaseProtocol(HostnameParserProtocol.INSTANCE, Range.lessThan(Integer.MIN_VALUE));
        ProtocolVersion.register(-2, "AUTO");

        //BlueberryLoader.getInstance().getEntrypoints("viablueberry:via_api_initialized", Runnable.class).forEach(Runnable::run);

        registerCommands(null);

        config = new VBConfig(new File(Blueberry.getConfigDir(), "ViaBlueberry/viablueberry.yml"));

        INIT_FUTURE.complete(null);
    }

    @EventHandler
    public void onCommandRegistration(CommandRegistrationEvent e) {
        registerCommands(e.getDispatcher());
    }

    private void registerCommands(@Nullable CommandDispatcher<CommandSourceStack> dispatcher) {
        if (dispatcher != null) {
            dispatcher.register(command("viaversion"));
            dispatcher.register(command("viaver"));
            dispatcher.register(command("vvblueberry"));
        }
        Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
            @Override
            public void execute() {
                ClientCommandManager.register("viaversion", dispatcher -> dispatcher.register(command("viaversion")));
                ClientCommandManager.register("viaver", dispatcher -> dispatcher.register(command("viaver")));
                ClientCommandManager.register("vvblueberry", dispatcher -> dispatcher.register(command("vvblueberry")));
            }
        });
    }
}
