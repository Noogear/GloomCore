package cn.gloomcore.scheduler.entity;

import cn.gloomcore.scheduler.common.Platform;
import cn.gloomcore.scheduler.common.Scheduler;
import cn.gloomcore.scheduler.common.Task;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public interface EntityScheduler extends Scheduler {
    static EntityScheduler get(Plugin plugin) {
        return Platform.FOLIA.isPlatform()
                ? new FoliaEntityScheduler(plugin)
                : new BukkitEntityScheduler(plugin);
    }

    static boolean isEntityValid(Entity entity) {
        if (entity == null) {
            return false;
        }
        if (entity instanceof Player player) {
            return player.isOnline();
        }
        return entity.isValid();
    }

    Task run(@Nullable Entity entity, @NotNull Runnable runnable, Runnable retired);

    Task runLater(@Nullable Entity entity, @NotNull Runnable runnable, Runnable retired, long delay);

    Task runTimer(@Nullable Entity entity, BooleanSupplier runnable, Runnable retired, long delay, long period);

    default Task runTimer(@NotNull Runnable runnable, Runnable retired, long delay, long period) {
        return runTimer(null, () -> {
            runnable.run();
            return true;
        }, retired, delay, period);
    }

    @Override
    default Task run(@NotNull Runnable runnable) {
        return run(null, runnable, () -> {
        });
    }

    @Override
    default Task runLater(@NotNull Runnable runnable, long delay) {
        return runLater(null, runnable, () -> {
        }, delay);
    }

    @Override
    default Task runTimer(@NotNull BooleanSupplier runnable, long delay, long period) {
        return runTimer(null, runnable, () -> {
        }, delay, period);
    }
}

