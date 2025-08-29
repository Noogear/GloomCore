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
 * 提供了对Paper线程调度API的便捷访问和封装
 */
public enum PaperScheduler {
    INSTANCE;

    private final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(this.getClass());
    private final AsyncScheduler asyncScheduler = plugin.getServer().getAsyncScheduler();
    private final Executor asyncExecutor = (runnable) -> runAsync(runnableToConsumer(runnable));
    private final GlobalRegionScheduler globalRegionScheduler = plugin.getServer().getGlobalRegionScheduler();
    private final Executor globalRegionExecutor = (runnable) -> run(runnableToConsumer(runnable));
    private final RegionScheduler regionScheduler = plugin.getServer().getRegionScheduler();

    /**
     * 获取异步执行器
     *
     * @return 异步执行器，用于在异步线程中执行任务
     */
    public @NotNull Executor asyncExecutor() {
        return asyncExecutor;
    }

    /**
     * 获取全局区域执行器
     *
     * @return 全局区域执行器，用于在全局区域线程中执行任务
     */
    public @NotNull Executor globalRegionExecutor() {
        return globalRegionExecutor;
    }

    /**
     * 获取指定位置的区域执行器
     *
     * @param location 位置信息
     * @return 区域执行器，用于在指定位置的区域线程中执行任务
     */
    public @NotNull Executor regionExecutor(@NotNull Location location) {
        return (runnable) -> run(location, runnableToConsumer(runnable));
    }

    /**
     * 获取指定世界的区块区域执行器
     *
     * @param world  世界对象
     * @param chunkX 区块X坐标
     * @param chunkZ 区块Z坐标
     * @return 区域执行器，用于在指定区块的区域线程中执行任务
     */
    public @NotNull Executor regionExecutor(@NotNull World world, int chunkX, int chunkZ) {
        return (runnable) -> run(world, chunkX, chunkZ, runnableToConsumer(runnable));
    }

    /**
     * 获取指定实体的执行器
     *
     * @param entity  实体对象
     * @param retired 实体退役时的回调任务
     * @return 实体执行器，用于在实体所在的区域线程中执行任务
     */
    public @NotNull Executor entityExecutor(@NotNull Entity entity, @Nullable Runnable retired) {
        return (runnable) -> run(entity, runnableToConsumer(runnable), retired);
    }

    /**
     * 获取指定实体的执行器
     *
     * @param entity 实体对象
     * @return 实体执行器，用于在实体所在的区域线程中执行任务
     */
    public @NotNull Executor entityExecutor(@NotNull Entity entity) {
        return entityExecutor(entity, null);
    }

    /**
     * 获取异步调度器
     *
     * @return 异步调度器，用于调度异步任务
     */
    public @NotNull AsyncScheduler asyncScheduler() {
        return asyncScheduler;
    }

    /**
     * 获取全局区域调度器
     *
     * @return 全局区域调度器，用于调度全局区域任务
     */
    public @NotNull GlobalRegionScheduler globalRegionScheduler() {
        return globalRegionScheduler;
    }

    /**
     * 获取区域调度器
     *
     * @return 区域调度器，用于调度区域任务
     */
    public @NotNull RegionScheduler regionScheduler() {
        return regionScheduler;
    }

    /**
     * 获取实体调度器
     *
     * @param entity 实体对象
     * @return 实体调度器，用于调度实体相关任务
     */
    public EntityScheduler entityScheduler(@NotNull Entity entity) {
        return entity.getScheduler();
    }

    /**
     * 在全局区域线程中立即执行任务
     *
     * @param consumer 任务消费者
     * @return 调度任务对象
     */
    public ScheduledTask run(@NotNull Consumer<ScheduledTask> consumer) {
        return globalRegionScheduler.run(plugin, consumer);
    }

    /**
     * 在指定位置的区域线程中立即执行任务
     *
     * @param location 位置信息
     * @param consumer 任务消费者
     * @return 调度任务对象
     */
    public ScheduledTask run(@NotNull Location location, @NotNull Consumer<ScheduledTask> consumer) {
        return regionScheduler.run(plugin, location, consumer);
    }

    /**
     * 在指定世界的区块区域线程中立即执行任务
     *
     * @param world    世界对象
     * @param chunkX   区块X坐标
     * @param chunkZ   区块Z坐标
     * @param consumer 任务消费者
     * @return 调度任务对象
     */
    public ScheduledTask run(@NotNull World world, int chunkX, int chunkZ, @NotNull Consumer<ScheduledTask> consumer) {
        return regionScheduler.run(plugin, world, chunkX, chunkZ, consumer);
    }

    /**
     * 在指定实体的线程中立即执行任务
     *
     * @param entity   实体对象
     * @param consumer 任务消费者
     * @param retired  实体退役时的回调任务
     * @return 调度任务对象
     */
    public ScheduledTask run(@NotNull Entity entity, @NotNull Consumer<ScheduledTask> consumer, @Nullable Runnable retired) {
        return entityScheduler(entity).run(plugin, consumer, retired);
    }

    /**
     * 在指定实体的线程中立即执行任务
     *
     * @param entity   实体对象
     * @param consumer 任务消费者
     * @return 调度任务对象
     */
    public ScheduledTask run(@NotNull Entity entity, @NotNull Consumer<ScheduledTask> consumer) {
        return run(entity, consumer, null);
    }

    /**
     * 在异步线程中立即执行任务
     *
     * @param consumer 任务消费者
     * @return 调度任务对象
     */
    public ScheduledTask runAsync(@NotNull Consumer<ScheduledTask> consumer) {
        return asyncScheduler.runNow(plugin, consumer);
    }

    /**
     * 在全局区域线程中延迟执行任务
     *
     * @param consumer 任务消费者
     * @param delay    延迟时间（tick）
     * @return 调度任务对象
     */
    public ScheduledTask runDelayed(@NotNull Consumer<ScheduledTask> consumer, long delay) {
        return globalRegionScheduler.runDelayed(plugin, consumer, toSafeTick(delay));
    }

    /**
     * 在指定位置的区域线程中延迟执行任务
     *
     * @param location 位置信息
     * @param consumer 任务消费者
     * @param delay    延迟时间（tick）
     * @return 调度任务对象
     */
    public ScheduledTask runDelayed(@NotNull Location location, @NotNull Consumer<ScheduledTask> consumer, long delay) {
        return regionScheduler.runDelayed(plugin, location, consumer, toSafeTick(delay));
    }

    /**
     * 在指定世界的区块区域线程中延迟执行任务
     *
     * @param world    世界对象
     * @param chunkX   区块X坐标
     * @param chunkZ   区块Z坐标
     * @param consumer 任务消费者
     * @param delay    延迟时间（tick）
     * @return 调度任务对象
     */
    public ScheduledTask runDelayed(@NotNull World world, int chunkX, int chunkZ, @NotNull Consumer<ScheduledTask> consumer, long delay) {
        return regionScheduler.runDelayed(plugin, world, chunkX, chunkZ, consumer, toSafeTick(delay));
    }

    /**
     * 在指定实体的线程中延迟执行任务
     *
     * @param entity   实体对象
     * @param consumer 任务消费者
     * @param retired  实体退役时的回调任务
     * @param delay    延迟时间（tick）
     * @return 调度任务对象
     */
    public ScheduledTask runDelayed(@NotNull Entity entity, @NotNull Consumer<ScheduledTask> consumer, @Nullable Runnable retired, long delay) {
        return entityScheduler(entity).runDelayed(plugin, consumer, retired, toSafeTick(delay));
    }

    /**
     * 在指定实体的线程中延迟执行任务
     *
     * @param entity   实体对象
     * @param consumer 任务消费者
     * @param delay    延迟时间（tick）
     * @return 调度任务对象
     */
    public ScheduledTask runDelayed(@NotNull Entity entity, @NotNull Consumer<ScheduledTask> consumer, long delay) {
        return runDelayed(entity, consumer, null, delay);
    }

    /**
     * 在异步线程中延迟执行任务
     *
     * @param consumer 任务消费者
     * @param delay    延迟时间（tick）
     * @return 调度任务对象
     */
    public ScheduledTask runDelayedAsync(@NotNull Consumer<ScheduledTask> consumer, long delay) {
        return asyncScheduler.runDelayed(plugin, consumer, toSafeTick(delay) * 50, TimeUnit.MILLISECONDS);
    }

    /**
     * 在全局区域线程中定时执行任务
     *
     * @param consumer 任务消费者
     * @param delay    初始延迟时间（tick）
     * @param period   执行周期（tick）
     * @return 调度任务对象
     */
    public ScheduledTask runTimer(@NotNull Consumer<ScheduledTask> consumer, long delay, long period) {
        return globalRegionScheduler.runAtFixedRate(plugin, consumer, toSafeTick(delay), toSafeTick(period));
    }

    /**
     * 在指定位置的区域线程中定时执行任务
     *
     * @param location 位置信息
     * @param consumer 任务消费者
     * @param delay    初始延迟时间（tick）
     * @param period   执行周期（tick）
     * @return 调度任务对象
     */
    public ScheduledTask runTimer(@NotNull Location location, @NotNull Consumer<ScheduledTask> consumer, long delay, long period) {
        return regionScheduler.runAtFixedRate(plugin, location, consumer, toSafeTick(delay), toSafeTick(period));
    }

    /**
     * 在指定世界的区块区域线程中定时执行任务
     *
     * @param world    世界对象
     * @param chunkX   区块X坐标
     * @param chunkZ   区块Z坐标
     * @param consumer 任务消费者
     * @param delay    初始延迟时间（tick）
     * @param period   执行周期（tick）
     * @return 调度任务对象
     */
    public ScheduledTask runTimer(@NotNull World world, int chunkX, int chunkZ, @NotNull Consumer<ScheduledTask> consumer, long delay, long period) {
        return regionScheduler.runAtFixedRate(plugin, world, chunkX, chunkZ, consumer, toSafeTick(delay), toSafeTick(period));
    }

    /**
     * 在指定实体的线程中定时执行任务
     *
     * @param entity   实体对象
     * @param consumer 任务消费者
     * @param retired  实体退役时的回调任务
     * @param delay    初始延迟时间（tick）
     * @param period   执行周期（tick）
     * @return 调度任务对象
     */
    public ScheduledTask runTimer(@NotNull Entity entity, @NotNull Consumer<ScheduledTask> consumer, @Nullable Runnable retired, long delay, long period) {
        return entityScheduler(entity).runAtFixedRate(plugin, consumer, retired, toSafeTick(delay), toSafeTick(period));
    }

    /**
     * 在指定实体的线程中定时执行任务
     *
     * @param entity   实体对象
     * @param consumer 任务消费者
     * @param delay    初始延迟时间（tick）
     * @param period   执行周期（tick）
     * @return 调度任务对象
     */
    public ScheduledTask runTimer(@NotNull Entity entity, @NotNull Consumer<ScheduledTask> consumer, long delay, long period) {
        return runTimer(entity, consumer, null, delay, period);
    }

    /**
     * 在异步线程中定时执行任务
     *
     * @param consumer 任务消费者
     * @param delay    初始延迟时间（tick）
     * @param period   执行周期（tick）
     * @return 调度任务对象
     */
    public ScheduledTask runTimerAsync(@NotNull Consumer<ScheduledTask> consumer, long delay, long period) {
        return asyncScheduler.runAtFixedRate(plugin, consumer, toSafeTick(delay) * 50, toSafeTick(period) * 50, TimeUnit.MILLISECONDS);
    }

    /**
     * 将Runnable转换为Consumer<ScheduledTask>
     *
     * @param runnable 可运行任务
     * @return 任务消费者
     */
    private Consumer<ScheduledTask> runnableToConsumer(Runnable runnable) {
        return (final ScheduledTask task) -> runnable.run();
    }

    /**
     * 确保tick值安全（大于0）
     *
     * @param originTick 原始tick值
     * @return 安全的tick值
     */
    private long toSafeTick(long originTick) {
        return originTick > 0 ? originTick : 1;
    }

}
