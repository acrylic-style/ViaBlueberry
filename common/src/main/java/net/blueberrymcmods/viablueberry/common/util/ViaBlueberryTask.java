package net.blueberrymcmods.viablueberry.common.util;

import com.viaversion.viaversion.api.platform.PlatformTask;
import net.blueberrymc.common.scheduler.BlueberryTask;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ViaBlueberryTask implements PlatformTask<BlueberryTask> {
    private final BlueberryTask task;

    public ViaBlueberryTask(BlueberryTask task) {
        this.task = task;
    }

    @Override
    public @Nullable BlueberryTask getObject() {
        return task;
    }

    @Override
    public void cancel() {
        task.cancel();
    }
}
