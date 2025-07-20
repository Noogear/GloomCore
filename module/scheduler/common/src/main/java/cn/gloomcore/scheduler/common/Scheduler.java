package cn.gloomcore.scheduler.common;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

public interface Scheduler {

    Task run(@NotNull Runnable runnable);

    Task runLater(@NotNull Runnable runnable, long delay);

    Task runTimer(@NotNull BooleanSupplier runnable, long delay, long period);

    default Task runTimer(Runnable runnable, long delay, long period) {
        return runTimer(() -> {
            runnable.run();
            return true;
        }, delay, period);
    }

    void cancelTasks();

    default void cancelTask(@NotNull Task task) {
        task.cancel();
    }

    default Executor getExecutor() {
        return this::run;
    }
}
