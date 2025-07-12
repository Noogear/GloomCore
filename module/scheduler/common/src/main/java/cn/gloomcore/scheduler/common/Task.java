package cn.gloomcore.scheduler.common;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Task {

    void cancel();

    @Nullable
    Integer taskId();

    boolean isCancelled();

    boolean isDone();

    @NotNull
    Plugin owner();
}
