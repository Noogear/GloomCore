package gloomcore.contract.builder;

import java.util.function.Consumer;

/**
 * 通用构建器接口
 *
 * @param <T> 实现此接口的具体构建器类型 (自限定泛型)
 * @param <R> 构建操作最终返回的产品类型
 */
public interface IBuilder<T extends IBuilder<T, R>, R> {
    /**
     * 构建并返回最终产品
     *
     * @return 构建完成的产品
     */
    R build();

    /**
     * 构建产品，并将其传递给消费者进行后续处理
     *
     * @param consumer 接受最终产品的消费者
     */
    default void buildAndThen(Consumer<R> consumer) {
        consumer.accept(build());
    }

    /**
     * 对构建器自身进行配置
     *
     * @param builderConsumer 配置逻辑
     * @return 当前构建器实例，用于链式调用
     */
    T configure(Consumer<T> builderConsumer);

    /**
     * 如果条件为真，则对构建器进行配置
     *
     * @param condition       判断条件
     * @param builderConsumer 如果条件为真，则执行的配置逻辑
     * @return 当前构建器实例
     */
    @SuppressWarnings("unchecked")
    default T configureIf(boolean condition, Consumer<T> builderConsumer) {
        if (condition) {
            final T self = (T) this;
            builderConsumer.accept(self);
        }
        return (T) this;
    }

    /**
     * 创建并返回当前构建器的一个副本
     * 实现者应确保这是一个深拷贝，以避免副作用
     *
     * @return 构建器的一个新实例，状态与当前实例相同
     */
    T copy();
}
