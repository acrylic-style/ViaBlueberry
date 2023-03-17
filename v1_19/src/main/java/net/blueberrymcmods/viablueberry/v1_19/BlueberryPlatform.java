package net.blueberrymcmods.viablueberry.v1_19;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import io.netty.channel.EventLoop;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.scheduler.BlueberryTask;
import net.blueberrymcmods.viablueberry.common.AbstractBlueberryPlatform;
import net.blueberrymcmods.viablueberry.common.commands.UserCommandSender;
import net.blueberrymcmods.viablueberry.common.util.FutureTaskId;
import net.blueberrymcmods.viablueberry.common.util.NativeVersionProvider;
import net.blueberrymcmods.viablueberry.common.util.ViaBlueberryTask;
import net.blueberrymcmods.viablueberry.v1_19.platform.BlueberryNativeVersionProvider;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class BlueberryPlatform extends AbstractBlueberryPlatform {
    @Override
    public ViaBlueberryTask runRepeatingAsync(Runnable runnable, long ticks) {
        BlueberryTask task = Blueberry.getUtil().getClientSchedulerOptional().get().runTaskTimerAsynchronously(ViaBlueberry.instance, 0, ticks, runnable);
        return new ViaBlueberryTask(task);
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
