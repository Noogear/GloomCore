package cn.gloomcore.scheduler.global;

import cn.gloomcore.scheduler.common.Task;
import cn.gloomcore.scheduler.common.task.BukkitTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

public class BukkitGlobalScheduler implements GlobalScheduler {
    private final Plugin plugin;

    BukkitGlobalScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Task run(@NotNull Runnable runnable) {
        return new BukkitTask(
                Bukkit.getScheduler().runTask(plugin, runnable)
        );
    }

    @Override
    public Task runLater(@NotNull Runnable runnable, long delay) {
        return new BukkitTask(
                Bukkit.getScheduler().runTaskLater(plugin, runnable, delay)
        );
    }


    @Override
    public Task runTimer(@NotNull BooleanSupplier runnable, long delay, long period) {
        return new BukkitTask(
                BukkitTask.wrapRunnable(runnable).runTaskTimer(plugin, delay, period)
        );
    }

    @Override
    public void cancelTasks() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }


}
