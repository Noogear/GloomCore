package gloomcore.contract.function;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 动作链执行器
 * 它持有一系列连接在一起的函数，并负责按顺序执行它们。
 * 这个类是不可变的，并且只能通过其内部的Builder来创建。
 *
 * @param <C> 初始上下文的类型
 * @param <R> 最终结果的类型
 */
public class FunctionChain<C, R> {

    private final List<Function> actions;

    // 1. 构造函数变为私有，强制使用Builder
    private FunctionChain(List<Function> actions) {
        // 使用不可变列表
        this.actions = List.copyOf(actions);
    }

    // 2. 提供一个静态工厂方法来获取新的Builder实例
    public static <T> Builder<T, T> newBuilder() {
        return new Builder<>();
    }

    /**
     * 执行整个动作链
     *
     * @param initialContext 初始输入上下文
     * @return 链条执行完毕后的最终结果
     */
    @SuppressWarnings("unchecked")
    public R execute(C initialContext) {
        Object currentState = initialContext;
        for (Function action : actions) {
            currentState = action.apply(currentState);
        }
        return (R) currentState;
    }

    // 3. 将原来的FunctionChainBuilder作为静态内部类

    /**
     * FunctionChain的建造者
     * 提供流畅的API来构建一个FunctionChain。
     *
     * @param <C> 初始上下文的类型
     * @param <R> 当前链条最后一个动作的输出类型
     */
    static class Builder<C, R> {

        private final List<Function> actions = new ArrayList<>();

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
            this.actions.add(nextFunction);
            // 类型转换是安全的，因为我们只是在更新泛型签名
            @SuppressWarnings("unchecked")
            Builder<C, NR> nextBuilder = (Builder<C, NR>) this;
            return nextBuilder;
        }

        /**
         * 完成构建，返回一个可执行的FunctionChain实例。
         *
         * @return 一个不可变的FunctionChain
         */
        public FunctionChain<C, R> build() {
            // 可以访问外部类的私有构造函数
            return new FunctionChain<>(actions);
        }
    }
}
