package cn.gloomcore.scheduler.entity;

import cn.gloomcore.scheduler.common.Task;
import cn.gloomcore.scheduler.common.task.FoliaTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

import static cn.gloomcore.scheduler.common.task.FoliaTask.runnableToConsumer;
import static cn.gloomcore.scheduler.common.task.FoliaTask.toSafeTick;

public class FoliaEntityScheduler implements EntityScheduler {
    private final Plugin plugin;

    public FoliaEntityScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Task run(@Nullable Entity entity, @NotNull Runnable runnable, Runnable retired) {
        ScheduledTask scheduledTask;
        if (EntityScheduler.isEntityValid(entity)) {
            scheduledTask = entity.getScheduler().run(plugin, runnableToConsumer(runnable), retired);
        } else {
            scheduledTask = Bukkit.getGlobalRegionScheduler().run(plugin, runnableToConsumer(retired));
        }
        return new FoliaTask(scheduledTask);
    }

    @Override
    public Task runLater(@Nullable Entity entity, @NotNull Runnable runnable, Runnable retired, long delay) {
        ScheduledTask scheduledTask;
        if (EntityScheduler.isEntityValid(entity)) {
            scheduledTask = entity.getScheduler().runDelayed(plugin, runnableToConsumer(runnable), retired, toSafeTick(delay));
        } else {
            scheduledTask = Bukkit.getGlobalRegionScheduler().runDelayed(plugin, runnableToConsumer(retired), toSafeTick(delay));
        }
        return new FoliaTask(scheduledTask);
    }

    @Override
    public Task runTimer(@Nullable Entity entity, BooleanSupplier runnable, Runnable retired, long delay, long period) {
        ScheduledTask scheduledTask;
        if (EntityScheduler.isEntityValid(entity)) {
            scheduledTask = entity.getScheduler().runAtFixedRate(plugin, runnableToConsumer(runnable), retired, toSafeTick(delay), toSafeTick(period));
        } else {
            scheduledTask = Bukkit.getGlobalRegionScheduler().runDelayed(plugin, runnableToConsumer(retired), toSafeTick(delay));
        }
        return new FoliaTask(scheduledTask);
    }

    @Override
    public void cancelTasks() {
        Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
    }

}
