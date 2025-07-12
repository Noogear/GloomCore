package cn.gloomcore.scheduler.common.task;

import cn.gloomcore.scheduler.common.Task;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class BukkitTask implements Task {
    private final org.bukkit.scheduler.BukkitTask bukkitTask;

    public BukkitTask(@NotNull org.bukkit.scheduler.BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }

    public static BukkitRunnable wrapRunnable(BooleanSupplier runnable) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (!runnable.getAsBoolean()) {
                    cancel();
                }
            }
        };
    }

    @Override
    public void cancel() {
        bukkitTask.cancel();
    }

    @Override
    public @Nullable Integer taskId() {
        return bukkitTask.getTaskId();
    }

    @Override
    public boolean isCancelled() {
        try {
            return bukkitTask.isCancelled();
        } catch (Throwable throwable) {
            int taskId = bukkitTask.getTaskId();
            return !Bukkit.getScheduler().isQueued(taskId) && !Bukkit.getScheduler().isCurrentlyRunning(taskId);
        }
    }

    @Override
    public boolean isDone() {
        try {
            if (bukkitTask.isCancelled()) {
                return false;
            }
        } catch (Throwable ignored) {
        }
        int taskId = bukkitTask.getTaskId();
        return !Bukkit.getScheduler().isQueued(taskId) && !Bukkit.getScheduler().isCurrentlyRunning(taskId);
    }

    @Override
    public @NotNull Plugin owner() {
        return bukkitTask.getOwner();
    }
}
