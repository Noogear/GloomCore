package cn.gloomcore.paper.scheduler;

import io.papermc.paper.threadedregions.scheduler.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Paper服务器线程调度器封装类
 * 提供统一的API来处理不同类型的调度任务，包括异步任务、全局任务、区域任务和实体任务
 */
public enum PaperScheduler {
    INSTANCE;

    private final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(this.getClass());
    private final AsyncScheduler asyncScheduler = plugin.getServer().getAsyncScheduler();
    private final GlobalRegionScheduler globalRegionScheduler = plugin.getServer().getGlobalRegionScheduler();
    private final RegionScheduler regionScheduler = plugin.getServer().getRegionScheduler();

    private final AsyncWrapper asyncWrapper = new AsyncWrapper();
    private final GlobalWrapper globalWrapper = new GlobalWrapper();

    /**
     * 将Runnable转换为Consumer<ScheduledTask>格式
     *
     * @param runnable 需要转换的Runnable任务
     * @return 转换后的Consumer<ScheduledTask>
     */
    private static Consumer<ScheduledTask> runnableToConsumer(Runnable runnable) {
        return task -> runnable.run();
    }

    /**
     * 确保tick值安全（至少为1）
     *
     * @param originTick 原始tick值
     * @return 安全的tick值
     */
    private static long toSafeTick(long originTick) {
        return Math.max(1, originTick);
    }

    /**
     * 获取异步任务调度器
     *
     * @return 异步任务调度器实例
     */
    public @NotNull AsyncWrapper async() {
        return asyncWrapper;
    }

    /**
     * 获取全局区域任务调度器
     *
     * @return 全局区域任务调度器实例
     */
    public @NotNull GlobalWrapper global() {
        return globalWrapper;
    }

    /**
     * 创建位置相关的区域任务调度器
     *
     * @param location 位置信息
     * @return 位置区域任务调度器实例
     */
    public @NotNull LocationWrapper location(@NotNull Location location) {
        return new LocationWrapper(location);
    }

    /**
     * 创建区块相关的区域任务调度器
     *
     * @param world  世界对象
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     * @return 区块区域任务调度器实例
     */
    public @NotNull ChunkWrapper chunk(@NotNull World world, int chunkX, int chunkZ) {
        return new ChunkWrapper(world, chunkX, chunkZ);
    }

    /**
     * 创建实体任务调度器
     *
     * @param entity 实体对象
     * @return 实体任务调度器实例
     */
    public @NotNull EntityWrapper entity(@NotNull Entity entity) {
        return new EntityWrapper(entity);
    }

    /**
     * 任务调度器接口
     * 定义了调度不同类型任务的通用接口
     */
    public interface TaskScheduler {
        /**
         * 立即运行任务
         *
         * @param task 要运行的任务
         * @return 调度任务对象
         */
        default ScheduledTask run(@NotNull Runnable task) {
            return run(runnableToConsumer(task));
        }

        /**
         * 立即运行任务
         *
         * @param task 要运行的任务
         * @return 调度任务对象
         */
        ScheduledTask run(@NotNull Consumer<ScheduledTask> task);

        /**
         * 延迟运行任务
         *
         * @param task  要运行的任务
         * @param delay 延迟时间（tick）
         * @return 调度任务对象
         */
        default ScheduledTask runDelayed(@NotNull Runnable task, long delay) {
            return runDelayed(runnableToConsumer(task), delay);
        }

        /**
         * 延迟运行任务
         *
         * @param task  要运行的任务
         * @param delay 延迟时间（tick）
         * @return 调度任务对象
         */
        ScheduledTask runDelayed(@NotNull Consumer<ScheduledTask> task, long delay);

        /**
         * 定时重复运行任务
         *
         * @param task   要运行的任务
         * @param delay  延迟时间（tick）
         * @param period 重复间隔（tick）
         * @return 调度任务对象
         */
        default ScheduledTask runTimer(@NotNull Runnable task, long delay, long period) {
            return runTimer(runnableToConsumer(task), delay, period);
        }

        /**
         * 定时重复运行任务
         *
         * @param task   要运行的任务
         * @param delay  延迟时间（tick）
         * @param period 重复间隔（tick）
         * @return 调度任务对象
         */
        ScheduledTask runTimer(@NotNull Consumer<ScheduledTask> task, long delay, long period);
    }

    /**
     * 异步任务调度器实现类
     * 处理在独立线程中运行的任务
     */
    public final class AsyncWrapper implements TaskScheduler {

        @Override
        public ScheduledTask run(@NotNull Consumer<ScheduledTask> task) {
            return asyncScheduler.runNow(plugin, task);
        }

        @Override
        public ScheduledTask runDelayed(@NotNull Consumer<ScheduledTask> task, long delay) {
            return asyncScheduler.runDelayed(plugin, task, toSafeTick(delay) * 50, TimeUnit.MILLISECONDS);
        }

        @Override
        public ScheduledTask runTimer(@NotNull Consumer<ScheduledTask> task, long delay, long period) {
            return asyncScheduler.runAtFixedRate(plugin, task, toSafeTick(delay) * 50, toSafeTick(period) * 50, TimeUnit.MILLISECONDS);
        }

        /**
         * 获取执行器
         *
         * @return 执行器对象
         */
        public @NotNull Executor executor() {
            return this::run;
        }
    }

    /**
     * 全局区域任务调度器实现类
     * 处理在整个服务器范围内运行的任务
     */
    public final class GlobalWrapper implements TaskScheduler {

        @Override
        public ScheduledTask run(@NotNull Consumer<ScheduledTask> task) {
            return globalRegionScheduler.run(plugin, task);
        }

        @Override
        public ScheduledTask runDelayed(@NotNull Consumer<ScheduledTask> task, long delay) {
            return globalRegionScheduler.runDelayed(plugin, task, toSafeTick(delay));
        }

        @Override
        public ScheduledTask runTimer(@NotNull Consumer<ScheduledTask> task, long delay, long period) {
            return globalRegionScheduler.runAtFixedRate(plugin, task, toSafeTick(delay), toSafeTick(period));
        }

        /**
         * 获取执行器
         *
         * @return 执行器对象
         */
        public @NotNull Executor executor() {
            return this::run;
        }
    }

    /**
     * 位置区域任务调度器实现类
     * 处理在特定位置区域运行的任务
     */
    public final class LocationWrapper implements TaskScheduler {
        private final Location location;

        private LocationWrapper(@NotNull Location location) {
            this.location = location;
        }

        @Override
        public ScheduledTask run(@NotNull Consumer<ScheduledTask> task) {
            return regionScheduler.run(plugin, location, task);
        }

        @Override
        public ScheduledTask runDelayed(@NotNull Consumer<ScheduledTask> task, long delay) {
            return regionScheduler.runDelayed(plugin, location, task, toSafeTick(delay));
        }

        @Override
        public ScheduledTask runTimer(@NotNull Consumer<ScheduledTask> task, long delay, long period) {
            return regionScheduler.runAtFixedRate(plugin, location, task, toSafeTick(delay), toSafeTick(period));
        }

        /**
         * 获取执行器
         *
         * @return 执行器对象
         */
        public @NotNull Executor executor() {
            return this::run;
        }
    }

    /**
     * 区块区域任务调度器实现类
     * 处理在特定区块区域运行的任务
     */
    public final class ChunkWrapper implements TaskScheduler {
        private final World world;
        private final int chunkX;
        private final int chunkZ;

        private ChunkWrapper(@NotNull World world, int chunkX, int chunkZ) {
            this.world = world;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        @Override
        public ScheduledTask run(@NotNull Consumer<ScheduledTask> task) {
            return regionScheduler.run(plugin, world, chunkX, chunkZ, task);
        }

        @Override
        public ScheduledTask runDelayed(@NotNull Consumer<ScheduledTask> task, long delay) {
            return regionScheduler.runDelayed(plugin, world, chunkX, chunkZ, task, toSafeTick(delay));
        }

        @Override
        public ScheduledTask runTimer(@NotNull Consumer<ScheduledTask> task, long delay, long period) {
            return regionScheduler.runAtFixedRate(plugin, world, chunkX, chunkZ, task, toSafeTick(delay), toSafeTick(period));
        }

        /**
         * 获取执行器
         *
         * @return 执行器对象
         */
        public @NotNull Executor executor() {
            return this::run;
        }
    }


    /**
     * 实体任务调度器实现类
     * 处理与特定实体相关的任务
     */
    public final class EntityWrapper implements TaskScheduler {
        private final EntityScheduler scheduler;

        private EntityWrapper(@NotNull Entity entity) {
            this.scheduler = entity.getScheduler();
        }

        /**
         * 运行任务，支持指定任务过期时的回调
         *
         * @param task    要运行的任务
         * @param retired 任务过期时的回调
         * @return 调度任务对象
         */
        public ScheduledTask run(@NotNull Runnable task, @Nullable Runnable retired) {
            return scheduler.run(plugin, runnableToConsumer(task), retired);
        }

        @Override
        public ScheduledTask run(@NotNull Consumer<ScheduledTask> task) {
            return scheduler.run(plugin, task, null);
        }

        @Override
        public ScheduledTask run(@NotNull Runnable task) {
            return run(task, null);
        }

        /**
         * 延迟运行任务，支持指定任务过期时的回调
         *
         * @param task    要运行的任务
         * @param retired 任务过期时的回调
         * @param delay   延迟时间（tick）
         * @return 调度任务对象
         */
        public ScheduledTask runDelayed(@NotNull Runnable task, @Nullable Runnable retired, long delay) {
            return scheduler.runDelayed(plugin, runnableToConsumer(task), retired, toSafeTick(delay));
        }

        @Override
        public ScheduledTask runDelayed(@NotNull Consumer<ScheduledTask> task, long delay) {
            return scheduler.runDelayed(plugin, task, null, toSafeTick(delay));
        }

        @Override
        public ScheduledTask runDelayed(@NotNull Runnable task, long delay) {
            return runDelayed(task, null, delay);
        }

        /**
         * 定时重复运行任务，支持指定任务过期时的回调
         *
         * @param task    要运行的任务
         * @param retired 任务过期时的回调
         * @param delay   延迟时间（tick）
         * @param period  重复间隔（tick）
         * @return 调度任务对象
         */
        public ScheduledTask runTimer(@NotNull Runnable task, @Nullable Runnable retired, long delay, long period) {
            return scheduler.runAtFixedRate(plugin, runnableToConsumer(task), retired, toSafeTick(delay), toSafeTick(period));
        }

        @Override
        public ScheduledTask runTimer(@NotNull Consumer<ScheduledTask> task, long delay, long period) {
            return scheduler.runAtFixedRate(plugin, task, null, toSafeTick(delay), toSafeTick(period));
        }

        @Override
        public ScheduledTask runTimer(@NotNull Runnable task, long delay, long period) {
            return runTimer(task, null, delay, period);
        }

        /**
         * 获取执行器，支持指定任务过期时的回调
         *
         * @param retired 任务过期时的回调
         * @return 执行器对象
         */
        public @NotNull Executor executor(@Nullable Runnable retired) {
            return (runnable) -> run(runnable, retired);
        }

        /**
         * 获取执行器
         *
         * @return 执行器对象
         */
        public @NotNull Executor executor() {
            return executor(null);
        }
    }
}
