package gloomcore.paper.contract.builder;

/**
 * 代表任何可以构建出类型 T 实例的构建器。
 * 这是构建器模式最基础的抽象。
 *
 * @param <T> 构建器最终产品的类型
 */
public interface Buildable<T> {

    /**
     * 完成构建过程并返回最终的产品实例。
     *
     * @return 构建完成的产品实例
     */
    T build();
}
