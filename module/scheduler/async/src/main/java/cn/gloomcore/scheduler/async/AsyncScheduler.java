package cn.gloomcore.scheduler.async;


import cn.gloomcore.scheduler.common.Platform;
import cn.gloomcore.scheduler.common.Scheduler;
import org.bukkit.plugin.Plugin;

public interface AsyncScheduler extends Scheduler {
    static AsyncScheduler get(Plugin plugin) {
        return Platform.FOLIA.isPlatform() ? new FoliaAsyncScheduler(plugin) : new BukkitAsyncScheduler(plugin);
    }

}
