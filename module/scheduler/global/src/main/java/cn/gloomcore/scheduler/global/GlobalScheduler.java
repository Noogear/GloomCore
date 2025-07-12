package cn.gloomcore.scheduler.global;

import cn.gloomcore.scheduler.common.Platform;
import cn.gloomcore.scheduler.common.Scheduler;
import org.bukkit.plugin.Plugin;

public interface GlobalScheduler extends Scheduler {
    static GlobalScheduler get(Plugin plugin) {
        return Platform.FOLIA.isPlatform() ? new FoliaGlobalScheduler(plugin) : new BukkitGlobalScheduler(plugin);
    }
}
