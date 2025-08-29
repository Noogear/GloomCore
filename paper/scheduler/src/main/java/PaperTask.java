import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import org.bukkit.plugin.java.JavaPlugin;

public enum PaperTask {
    INSTANCE;

    private final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(this.getClass());;
    private final AsyncScheduler asyncScheduler = plugin.getServer().getAsyncScheduler();


}
