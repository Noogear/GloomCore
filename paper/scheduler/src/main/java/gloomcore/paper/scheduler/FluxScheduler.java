package gloomcore.paper.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * FluxScheduler是一个高效的任务调度器，旨在优化周期性任务的执行。
 * 它通过将任务分配到不同的相位来平衡负载，避免所有任务同时运行造成的性能峰值。
 */
public enum FluxScheduler {
    INSTANCE;
    private static final long MERGE_THRESHOLD_TICKS = 100;
    private static final double GOLDEN_RATIO = (1 + Math.sqrt(5)) / 2;
    private static final PaperScheduler.AsyncWrapper ASYNC_WRAPPER = PaperScheduler.INSTANCE.async();
    private static final PaperScheduler.GlobalWrapper GLOBAL_WRAPPER = PaperScheduler.INSTANCE.global();
    private final Map<Long, List<Long>> phaseDelayCache = new ConcurrentHashMap<>();
    private final Map<SchedulerKey, PhasedTaskManager> phasedManagers = new ConcurrentHashMap<>();
    private final Map<Runnable, Consumer<Runnable>> taskRegistry = new ConcurrentHashMap<>();

    /**
     * 调度一个周期性任务
     *
     * @param task     要执行的任务
     * @param interval 执行间隔（tick）
     * @param isAsync  是否异步执行
     * @param strategy 调度策略
     */
    public void schedule(Runnable task, long interval, boolean isAsync, Strategy strategy) {
        // 防止重复注册同一个任务
        if (taskRegistry.containsKey(task)) {
            return;
        }
        dispatch(task, interval, isAsync, strategy);
    }

    /**
     * 根据任务特性选择合适的调度方式
     *
     * @param task     要调度的任务
     * @param interval 执行间隔
     * @param isAsync  是否异步执行
     * @param strategy 调度策略
     */
    private void dispatch(Runnable task, long interval, boolean isAsync, Strategy strategy) {
        if (interval > 0 && interval <= MERGE_THRESHOLD_TICKS) {
            SchedulerKey key = new SchedulerKey(interval, isAsync);
            List<Long> sortedDelays = phaseDelayCache.computeIfAbsent(interval, currentInterval -> {
                int maxPhases = 12;
                int phaseCount = (int) Math.min(maxPhases, Math.max(1, Math.round(Math.sqrt(currentInterval))));
                return IntStream.range(0, phaseCount)
                        .mapToLong(phase -> {
                            double fraction = (phase * GOLDEN_RATIO) % 1.0;
                            return Math.round(fraction * currentInterval);
                        })
                        .sorted()
                        .boxed()
                        .collect(Collectors.toList());
            });
            PhasedTaskManager manager = phasedManagers.computeIfAbsent(
                    key,
                    k -> new PhasedTaskManager(k.interval, k.isAsync, sortedDelays)
            );
            taskRegistry.put(task, manager::removeTask);
            manager.addTask(task, strategy);
        } else {
            ScheduledTask scheduledTask = isAsync
                    ? ASYNC_WRAPPER.runTimer(task, 0, interval)
                    : GLOBAL_WRAPPER.runTimer(task, 0, interval);
            taskRegistry.put(task, r -> scheduledTask.cancel());
        }
    }

    /**
     * 取消指定任务的调度
     *
     * @param task 要取消的任务
     */
    public void cancel(Runnable task) {
        Consumer<Runnable> cancellation = taskRegistry.remove(task);
        if (cancellation != null) {
            cancellation.accept(task);
        }
    }

    /**
     * 关闭调度器，取消所有任务并清理资源
     */
    public void shutdown() {
        // 取消所有分相任务管理器中的任务
        phasedManagers.values().forEach(PhasedTaskManager::cancelAll);
        phasedManagers.clear();

        // 取消所有已注册的任务
        Set<Runnable> tasksToCancel = new HashSet<>(taskRegistry.keySet());
        tasksToCancel.forEach(this::cancel);

        taskRegistry.clear();
        phaseDelayCache.clear(); // 在关闭时清空缓存
    }

    /**
     * 任务调度策略枚举
     */
    public enum Strategy {
        /**
         * 负载均衡：随机分配任务到不同相位，平衡各相位负载
         */
        LOAD_BALANCE,
        /**
         * 最小延迟：将任务分配到下一个将要执行的相位，减少等待时间
         */
        MINIMIZE_LATENCY
    }

    /**
     * 调度器键：用于唯一标识具有相同间隔和执行模式的任务组
     *
     * @param interval 执行间隔
     * @param isAsync  是否异步执行
     */
    private record SchedulerKey(long interval, boolean isAsync) {
    }

    /**
     * 分相任务管理器：管理同一间隔下的多个任务组（相位）
     * 每个相位在不同的时间点执行，避免所有任务同时运行造成的性能峰值
     */
    private static final class PhasedTaskManager {
        private final List<TaskGroup> groupsByPhase;
        private final Map<Runnable, TaskGroup> taskToGroupMap = new ConcurrentHashMap<>();
        private volatile int nextToExecuteIndex = 0;

        /**
         * 构造函数现在接收预先计算和排序好的延迟列表
         *
         * @param interval     任务执行间隔
         * @param isAsync      是否异步执行
         * @param sortedDelays 已排序的相位延迟列表
         */
        PhasedTaskManager(long interval, boolean isAsync, List<Long> sortedDelays) {
            this.groupsByPhase = IntStream.range(0, sortedDelays.size())
                    .mapToObj(index -> {
                        long delay = sortedDelays.get(index);
                        return new TaskGroup(interval, delay, isAsync, index, this::onGroupExecuted);
                    })
                    .collect(Collectors.toList());
        }

        /**
         * 根据策略将任务添加到合适的任务组
         *
         * @param task     要添加的任务
         * @param strategy 调度策略
         */
        void addTask(Runnable task, Strategy strategy) {
            TaskGroup targetGroup;
            if (strategy == Strategy.MINIMIZE_LATENCY) {
                targetGroup = groupsByPhase.get(nextToExecuteIndex);
            } else {
                int index = ThreadLocalRandom.current().nextInt(groupsByPhase.size());
                targetGroup = groupsByPhase.get(index);
            }

            taskToGroupMap.put(task, targetGroup);
            targetGroup.addTask(task);
        }

        /**
         * 从管理器中移除指定任务
         *
         * @param task 要移除的任务
         */
        void removeTask(Runnable task) {
            TaskGroup group = taskToGroupMap.remove(task);
            if (group != null) {
                group.removeTask(task);
            }
        }

        /**
         * 任务组执行完毕后的回调，更新下一个执行索引
         *
         * @param justExecutedGroup 刚执行完的任务组
         */
        private void onGroupExecuted(TaskGroup justExecutedGroup) {
            this.nextToExecuteIndex = (justExecutedGroup.getPhaseIndex() + 1) % groupsByPhase.size();
        }

        /**
         * 取消所有任务组中的任务
         */
        void cancelAll() {
            groupsByPhase.forEach(TaskGroup::cancel);
            taskToGroupMap.clear();
        }
    }

    /**
     * 任务组：代表一个特定相位的任务集合
     * 同一任务组中的所有任务会在相同的时间点同时执行
     */
    private static final class TaskGroup {
        private final long interval;
        private final long delay;
        private final boolean isAsync;
        private final int phaseIndex;
        private final Consumer<TaskGroup> onExecuteCallback;
        private final Set<Runnable> tasks = ConcurrentHashMap.newKeySet();
        private ScheduledTask scheduledTask;

        /**
         * 创建一个新的任务组
         *
         * @param interval          执行间隔
         * @param delay             相位延迟
         * @param isAsync           是否异步执行
         * @param phaseIndex        相位索引
         * @param onExecuteCallback 执行回调
         */
        TaskGroup(long interval, long delay, boolean isAsync, int phaseIndex, Consumer<TaskGroup> onExecuteCallback) {
            this.interval = interval;
            this.delay = delay;
            this.isAsync = isAsync;
            this.phaseIndex = phaseIndex;
            this.onExecuteCallback = onExecuteCallback;
        }

        /**
         * 向任务组添加新任务
         *
         * @param task 要添加的任务
         */
        void addTask(Runnable task) {
            tasks.add(task);
            // 如果当前没有运行的调度任务，则启动
            if (scheduledTask == null || scheduledTask.isCancelled()) {
                start();
            }
        }

        /**
         * 从任务组移除任务
         *
         * @param task 要移除的任务
         */
        void removeTask(Runnable task) {
            tasks.remove(task);
            // 如果没有任务了，取消调度
            if (tasks.isEmpty() && scheduledTask != null && !scheduledTask.isCancelled()) {
                scheduledTask.cancel();
            }
        }

        /**
         * 获取任务组的相位索引
         *
         * @return 相位索引
         */
        int getPhaseIndex() {
            return this.phaseIndex;
        }

        /**
         * 启动任务组的周期性执行
         */
        private void start() {
            Runnable runnable = () -> {
                onExecuteCallback.accept(TaskGroup.this);
                tasks.forEach(Runnable::run);
            };
            this.scheduledTask = isAsync
                    ? ASYNC_WRAPPER.runTimer(runnable, delay, interval)
                    : GLOBAL_WRAPPER.runTimer(runnable, delay, interval);
        }

        /**
         * 取消任务组中的所有任务
         */
        void cancel() {
            if (scheduledTask != null && !scheduledTask.isCancelled()) {
                scheduledTask.cancel();
            }
            tasks.clear();
        }
    }
}

