package net.blueberrymcmods.viablueberry.common.util;

import com.viaversion.viaversion.api.platform.PlatformTask;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;

public class FutureTaskId implements PlatformTask<Future<?>> {
    private final Future<?> future;

    public FutureTaskId(@NotNull Future<?> future) {
        this.future = future;
    }

    @Override
    public @Nullable Future<?> getObject() {
        return future;
    }

    @Override
    public void cancel() {
        future.cancel(true);
    }
}
