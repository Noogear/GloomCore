package gloomcore.paper.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Flux调度器，用于管理和调度任务
 * 提供了任务合并和相位管理功能，以优化调度性能
 */
public enum FluxScheduler {
    INSTANCE;

    private static final long MERGE_THRESHOLD_TICKS = 100;
    private static final PaperScheduler.AsyncWrapper ASYNC_WRAPPER = PaperScheduler.INSTANCE.async();
    private static final PaperScheduler.GlobalWrapper GLOBAL_WRAPPER = PaperScheduler.INSTANCE.global();
    private final Map<SchedulerKey, PhasedTaskManager> phasedManagers = new ConcurrentHashMap<>();
    private final Map<Runnable, Consumer<Runnable>> taskRegistry = new ConcurrentHashMap<>();

    /**
     * 调度一个任务
     *
     * @param task     要调度的Runnable任务
     * @param interval 任务执行间隔（以tick为单位）
     * @param isAsync  是否异步执行任务
     * @param strategy 任务调度策略
     */
    public void schedule(Runnable task, long interval, boolean isAsync, Strategy strategy) {
        if (taskRegistry.containsKey(task)) {
            return;
        }
        dispatch(task, interval, isAsync, strategy);
    }

    /**
     * 分发任务到合适的任务管理器
     *
     * @param task     要调度的Runnable任务
     * @param interval 任务执行间隔（以tick为单位）
     * @param isAsync  是否异步执行任务
     * @param strategy 任务调度策略
     */
    private void dispatch(Runnable task, long interval, boolean isAsync, Strategy strategy) {
        if (interval <= MERGE_THRESHOLD_TICKS) {
            SchedulerKey key = new SchedulerKey(interval, isAsync);

            PhasedTaskManager manager = phasedManagers.computeIfAbsent(
                    key,
                    k -> new PhasedTaskManager(k.interval, (int) Math.min(4, Math.max(1, interval)), k.isAsync)
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
     * 取消指定的任务
     *
     * @param task 要取消的Runnable任务
     */
    public void cancel(Runnable task) {
        Consumer<Runnable> cancellation = taskRegistry.remove(task);
        if (cancellation != null) {
            cancellation.accept(task);
        }
    }

    /**
     * 关闭调度器，取消所有已注册的任务
     */
    public void shutdown() {
        phasedManagers.values().forEach(PhasedTaskManager::cancelAll);
        phasedManagers.clear();

        taskRegistry.forEach((task, cancellation) -> cancellation.accept(task));
        taskRegistry.clear();
    }

    /**
     * 任务调度策略枚举
     */
    public enum Strategy {
        /**
         * 负载均衡策略，在各相位间均匀分配任务
         */
        LOAD_BALANCE,
        /**
         * 最小化延迟策略，将任务分配到即将执行的相位组中
         */
        MINIMIZE_LATENCY
    }

    /**
     * 调度键，用于标识具有相同间隔和执行方式的任务组
     */
    private record SchedulerKey(long interval, boolean isAsync) {
    }

    /**
     * 相位任务管理器，负责管理具有相同调度参数但不同相位的任务组
     */
    private static final class PhasedTaskManager {
        private final List<TaskGroup> groupsByPhase;
        private final Map<Runnable, TaskGroup> taskToGroupMap = new ConcurrentHashMap<>();
        private final AtomicInteger roundRobinIndex = new AtomicInteger(0);
        private volatile int nextToExecuteIndex = 0;

        /**
         * 构造一个新的相位任务管理器
         *
         * @param interval   任务执行间隔
         * @param phaseCount 相位数量
         * @param isAsync    是否异步执行
         */
        PhasedTaskManager(long interval, int phaseCount, boolean isAsync) {
            this.groupsByPhase = IntStream.range(0, phaseCount)
                    .mapToObj(phase -> new TaskGroup(
                            interval,
                            Math.round((double) phase * interval / phaseCount),
                            isAsync,
                            this::onGroupExecuted
                    ))
                    .collect(Collectors.toList());
        }

        /**
         * 添加任务到适当的相位组中
         *
         * @param task     要添加的任务
         * @param strategy 调度策略
         */
        void addTask(Runnable task, Strategy strategy) {
            TaskGroup targetGroup = (strategy == Strategy.MINIMIZE_LATENCY)
                    ? groupsByPhase.get(nextToExecuteIndex)
                    : groupsByPhase.get(roundRobinIndex.getAndIncrement() % groupsByPhase.size());

            taskToGroupMap.put(task, targetGroup);
            targetGroup.addTask(task);
        }

        /**
         * 从相位组中移除任务
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
         * 当一个任务组执行完毕后的回调方法
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
     * 任务组，包含具有相同调度参数和延迟的一组任务
     */
    private static final class TaskGroup {
        private final long interval;
        private final long delay;
        private final boolean isAsync;
        private final int phaseIndex;
        private final Consumer<TaskGroup> onExecuteCallback;
        private final CopyOnWriteArraySet<Runnable> tasks = new CopyOnWriteArraySet<>();
        private ScheduledTask scheduledTask;

        /**
         * 构造一个新的任务组
         *
         * @param interval          任务执行间隔
         * @param delay             任务延迟
         * @param isAsync           是否异步执行
         * @param onExecuteCallback 任务组执行时的回调函数
         */
        TaskGroup(long interval, long delay, boolean isAsync, Consumer<TaskGroup> onExecuteCallback) {
            this.interval = interval;
            this.delay = delay;
            this.isAsync = isAsync;
            this.phaseIndex = (int) (delay * 1.0 / interval);
            this.onExecuteCallback = onExecuteCallback;
        }

        /**
         * 向任务组中添加任务
         *
         * @param task 要添加的任务
         */
        void addTask(Runnable task) {
            tasks.add(task);
            if (scheduledTask == null || scheduledTask.isCancelled()) {
                start();
            }
        }

        /**
         * 从任务组中移除任务
         *
         * @param task 要移除的任务
         */
        void removeTask(Runnable task) {
            tasks.remove(task);
            if (tasks.isEmpty() && scheduledTask != null && !scheduledTask.isCancelled()) {
                scheduledTask.cancel();
            }
        }

        /**
         * 获取相位索引
         *
         * @return 相位索引
         */
        int getPhaseIndex() {
            return this.phaseIndex;
        }

        /**
         * 启动任务组的定时执行
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
         * 取消任务组的所有任务
         */
        void cancel() {
            if (scheduledTask != null && !scheduledTask.isCancelled()) {
                scheduledTask.cancel();
            }
            tasks.clear();
        }
    }
}
