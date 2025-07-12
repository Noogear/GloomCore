package cn.gloomcore.scheduler.async;

import cn.gloomcore.scheduler.common.Task;
import cn.gloomcore.scheduler.common.task.BukkitTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

public class BukkitAsyncScheduler implements AsyncScheduler {
    private final Plugin plugin;

    public BukkitAsyncScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Task run(@NotNull Runnable runnable) {
        return new BukkitTask(
                Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable)
        );
    }

    @Override
    public Task runLater(@NotNull Runnable runnable, long delay) {
        return new BukkitTask(
                Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay)
        );
    }

    @Override
    public Task runTimer(@NotNull BooleanSupplier runnable, long delay, long period) {
        return new BukkitTask(
                BukkitTask.wrapRunnable(runnable).runTaskTimerAsynchronously(plugin, delay, period)
        );
    }

    @Override
    public void cancelTasks() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }

}
