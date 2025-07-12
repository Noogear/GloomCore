package cn.gloomcore.scheduler.common.task;

import cn.gloomcore.scheduler.common.Task;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class FoliaTask implements Task {
    private final ScheduledTask scheduledTask;

    public FoliaTask(ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    public static Consumer<ScheduledTask> runnableToConsumer(Runnable runnable) {
        return (final ScheduledTask task) -> runnable.run();
    }

    public static Consumer<ScheduledTask> runnableToConsumer(BooleanSupplier runnable) {
        return (final ScheduledTask task) -> {
            if (!runnable.getAsBoolean()) {
                task.cancel();
            }
        };
    }

    public static long toSafeTick(long originTick) {
        return originTick > 0 ? originTick : 1;
    }

    @Override
    public void cancel() {
        scheduledTask.cancel();
    }

    @Override
    public @Nullable Integer taskId() {
        return null;
    }

    @Override
    public boolean isCancelled() {
        return scheduledTask.isCancelled();
    }

    @Override
    public boolean isDone() {
        return scheduledTask.getExecutionState() == ScheduledTask.ExecutionState.FINISHED;
    }

    @Override
    public @NotNull Plugin owner() {
        return scheduledTask.getOwningPlugin();
    }
}
