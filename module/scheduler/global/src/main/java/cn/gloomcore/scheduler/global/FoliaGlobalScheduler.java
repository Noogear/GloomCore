package cn.gloomcore.scheduler.global;

import cn.gloomcore.scheduler.common.Task;
import cn.gloomcore.scheduler.common.task.FoliaTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

import static cn.gloomcore.scheduler.common.task.FoliaTask.runnableToConsumer;
import static cn.gloomcore.scheduler.common.task.FoliaTask.toSafeTick;

public class FoliaGlobalScheduler implements GlobalScheduler {
    private final Plugin plugin;

    public FoliaGlobalScheduler(Plugin plugin) {
        this.plugin = plugin;
    }


    @Override
    public Task run(@NotNull Runnable runnable) {
        return new FoliaTask(Bukkit.getGlobalRegionScheduler().run(plugin, runnableToConsumer(runnable)));
    }

    @Override
    public Task runLater(@NotNull Runnable runnable, long delay) {
        return new FoliaTask(Bukkit.getGlobalRegionScheduler().runDelayed(plugin, runnableToConsumer(runnable), toSafeTick(delay)));
    }

    @Override
    public Task runTimer(@NotNull BooleanSupplier runnable, long delay, long period) {
        return new FoliaTask(
                Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, runnableToConsumer(runnable), toSafeTick(delay), toSafeTick(period))
        );
    }

    @Override
    public void cancelTasks() {

    }
}
