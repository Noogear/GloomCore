package cn.gloomcore.scheduler.entity;

import cn.gloomcore.scheduler.common.Task;
import cn.gloomcore.scheduler.common.task.BukkitTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class BukkitEntityScheduler implements EntityScheduler {
    private final Plugin plugin;

    public BukkitEntityScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    private BukkitRunnable wrapRunnable(BooleanSupplier runnable, Runnable retired, Entity entity) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (EntityScheduler.isEntityValid(entity)) {
                    if (!runnable.getAsBoolean()) {
                        cancel();
                    }
                } else {
                    retired.run();
                    cancel();
                }
            }
        };
    }

    private BukkitRunnable wrapRunnable(Runnable runnable, Runnable retired, Entity entity) {
        return wrapRunnable(() -> {
            runnable.run();
            return true;
        }, retired, entity);
    }


    @Override
    public Task run(@Nullable Entity entity, @NotNull Runnable runnable, Runnable retired) {
        return new BukkitTask(
                wrapRunnable(runnable, retired, entity).runTask(plugin)
        );
    }

    @Override
    public Task runLater(@Nullable Entity entity, @NotNull Runnable runnable, Runnable retired, long delay) {
        return new BukkitTask(
                wrapRunnable(runnable, retired, entity).runTaskLater(plugin, delay)
        );
    }

    @Override
    public Task runTimer(@Nullable Entity entity, BooleanSupplier runnable, Runnable retired, long delay, long period) {
        return new BukkitTask(
                wrapRunnable(runnable, retired, entity).runTaskTimer(plugin, delay, period)
        );
    }

    @Override
    public void cancelTasks() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }

}
