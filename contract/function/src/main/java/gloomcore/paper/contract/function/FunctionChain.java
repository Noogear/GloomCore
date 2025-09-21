package gloomcore.paper.contract.function;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 动作链执行器
 * 它持有一系列连接在一起的函数，并负责按顺序执行它们。
 * 这个类是不可变的，并且只能通过其内部的Builder来创建。
 *
 * @param <C> 初始上下文的类型
 * @param <R> 最终结果的类型
 */
public final class FunctionChain<C, R> implements Function<C, R> {

    private final List<Function> actions;
    private final Function<Throwable, R> errorHandler;

    /**
     * 私有构造函数，强制使用Builder。
     *
     * @param functions    要执行的函数列表。
     * @param errorHandler 发生异常时的处理器。
     */
    private FunctionChain(List<Function> functions, Function<Throwable, R> errorHandler) {
        // 使用不可变列表，确保线程安全和不可变性
        this.actions = List.copyOf(functions);
        this.errorHandler = errorHandler;
    }

    /**
     * 创建一个新的建造者实例，用于构建函数链。
     *
     * @param <T> 链条的初始输入类型。
     * @return 一个新的Builder实例。
     */
    public static <T> Builder<T, T> newBuilder() {
        return new Builder<>();
    }

    /**
     * 从一个已有的Function开始构建一个新的链条。
     *
     * @param initialFunction 链条的第一个动作。
     * @param <C>             初始输入类型。
     * @param <R>             第一个动作的返回类型。
     * @return 一个已经包含初始动作的Builder实例。
     */
    public static <C, R> Builder<C, R> from(Function<C, R> initialFunction) {
        Builder<C, R> builder = new Builder<>();
        builder.functions.add(initialFunction);
        return builder;
    }

    /**
     * 执行整个动作链。
     * 如果任何一个动作抛出异常，并且设置了错误处理器，则会调用错误处理器并返回其结果。
     * 否则，异常将向上抛出。
     *
     * @param initialContext 初始输入上下文
     * @return 链条执行完毕后的最终结果，或者错误处理器的返回结果。
     */
    @Override
    @SuppressWarnings("unchecked")
    public R apply(C initialContext) {
        try {
            Object currentState = initialContext;
            for (Function action : actions) {
                currentState = action.apply(currentState);
            }
            return (R) currentState;
        } catch (Exception e) {
            if (errorHandler != null) {
                return errorHandler.apply(e);
            }
            // 如果没有错误处理器，则重新抛出原始异常
            throw new RuntimeException("FunctionChain execution failed", e);
        }
    }

    /**
     * FunctionChain的建造者
     * 提供流畅的API来构建一个FunctionChain。
     *
     * @param <C> 初始上下文的类型
     * @param <R> 当前链条最后一个动作的输出类型
     */
    public static final class Builder<C, R> {

        private final List<Function> functions = new ArrayList<>();
        private Function<Throwable, R> errorHandler;

        // 私有构造函数，通过外部类的静态方法创建
        private Builder() {
        }

        /**
         * 向链条中添加一个新动作。
         *
         * @param nextFunction 要添加的动作（函数）
         * @param <NR>         新动作的返回类型，也将成为链条新的最终返回类型
         * @return 更新后的建造者，其最终返回类型为新动作的返回类型
         */
        public <NR> Builder<C, NR> add(Function<R, NR> nextFunction) {
            this.functions.add(nextFunction);
            // 类型转换是安全的，因为我们只是在更新泛型签名
            @SuppressWarnings("unchecked")
            Builder<C, NR> nextBuilder = (Builder<C, NR>) this;
            return nextBuilder;
        }

        /**
         * 将另一个完整的FunctionChain连接到当前链条的末尾。
         *
         * @param nextChain 要连接的函数链。
         * @param <NR>      被连接链条的最终返回类型。
         * @return 更新后的建造者。
         */
        public <NR> Builder<C, NR> add(FunctionChain<R, NR> nextChain) {
            this.functions.addAll(nextChain.actions);
            @SuppressWarnings("unchecked")
            Builder<C, NR> nextBuilder = (Builder<C, NR>) this;
            return nextBuilder;
        }

        /**
         * 添加一个“窥视”动作，用于调试。
         * 此动作会消费当前链条的结果，但不会改变它，结果会原封不动地传递给下一个动作。
         *
         * @param inspector 用于检查/消费当前结果的Consumer。
         * @return 当前建造者实例，类型不变。
         */
        public Builder<C, R> peek(Consumer<R> inspector) {
            this.functions.add(currentValue -> {
                // 类型转换是安全的，因为Builder的类型系统保证了这里的类型正确性
                inspector.accept((R) currentValue);
                return currentValue;
            });
            return this;
        }

        /**
         * 设置一个全局错误处理器。当链条中任何函数抛出异常时，该处理器将被调用。
         * 注意：一旦设置，后续的add操作如果改变了最终返回类型R，可能会导致类型不匹配。
         * 建议在链条构建的最后阶段设置错误处理器。
         *
         * @param handler 异常处理器，接收一个Throwable并返回一个默认/备用结果。
         * @return 当前建造者实例。
         */
        public Builder<C, R> onError(Function<Throwable, R> handler) {
            this.errorHandler = handler;
            return this;
        }

        /**
         * 完成构建，返回一个可执行的FunctionChain实例。
         *
         * @return 一个不可变的FunctionChain
         */
        public FunctionChain<C, R> build() {
            return new FunctionChain<>(functions, errorHandler);
        }
    }
}