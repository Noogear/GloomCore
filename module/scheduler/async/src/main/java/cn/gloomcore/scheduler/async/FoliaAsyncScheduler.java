package cn.gloomcore.scheduler.async;

import cn.gloomcore.scheduler.common.Task;
import cn.gloomcore.scheduler.common.task.FoliaTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static cn.gloomcore.scheduler.common.task.FoliaTask.runnableToConsumer;
import static cn.gloomcore.scheduler.common.task.FoliaTask.toSafeTick;

public class FoliaAsyncScheduler implements AsyncScheduler {
    private final Plugin plugin;
    private final io.papermc.paper.threadedregions.scheduler.AsyncScheduler asyncScheduler;

    public FoliaAsyncScheduler(Plugin plugin) {
        this.plugin = plugin;
        asyncScheduler = Bukkit.getAsyncScheduler();
    }

    @Override
    public Task run(@NotNull Runnable runnable) {
        return new FoliaTask(
                asyncScheduler.runNow(plugin, runnableToConsumer(runnable))
        );
    }

    @Override
    public Task runLater(@NotNull Runnable runnable, long delay) {
        return new FoliaTask(
                asyncScheduler.runDelayed(plugin, runnableToConsumer(runnable), toSafeTick(delay) * 50, TimeUnit.MILLISECONDS)
        );
    }

    @Override
    public Task runTimer(@NotNull BooleanSupplier runnable, long delay, long period) {
        return new FoliaTask(
                asyncScheduler.runAtFixedRate(plugin, runnableToConsumer(runnable), toSafeTick(delay) * 50, toSafeTick(period) * 50, TimeUnit.MILLISECONDS)
        );
    }

    @Override
    public void cancelTasks() {
        asyncScheduler.cancelTasks(plugin);
    }

}
