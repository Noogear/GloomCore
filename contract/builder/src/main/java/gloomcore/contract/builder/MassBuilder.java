package gloomcore.contract.builder;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 一个可以从单个输入构建出多个输出的建造者。
 * <p>
 * 其构建元素是一个 {@link Function}，它接收一个输入并返回一个 {@link Optional} 类型的输出。
 * 如果该构建元素无法处理给定的输入，其返回的 {@link Optional} 就应该为空。
 *
 * @param <I> 输入值的类型
 * @param <O> 输出值的类型
 */
public class MassBuilder<I, O> {
    private final ArrayDeque<Function<I, Optional<O>>> elements = new ArrayDeque<>();

    /**
     * 注册一个新的构建元素。
     *
     * @param element  要注册的构建元素
     * @param addFirst 如果为 true，则将元素添加到处理队列的头部（优先处理）
     * @return 当前建造者实例，用于链式调用
     */
    public MassBuilder<I, O> register(Function<I, Optional<O>> element, boolean addFirst) {
        if (addFirst) {
            elements.addFirst(element);
        } else {
            elements.addLast(element);
        }
        return this;
    }

    /**
     * 注册一个新的构建元素到处理队列的末尾。
     *
     * @param element 要注册的构建元素
     * @return 当前建造者实例，用于链式调用
     */
    public MassBuilder<I, O> register(Function<I, Optional<O>> element) {
        return register(element, false);
    }

    /**
     * 移除一个已注册的构建元素。
     *
     * @param element 要移除的构建元素
     * @return 当前建造者实例，用于链式调用
     */
    public MassBuilder<I, O> remove(Function<I, Optional<O>> element) {
        elements.remove(element);
        return this;
    }

    /**
     * 清空所有已注册的构建元素。
     *
     * @return 当前建造者实例，用于链式调用
     */
    public MassBuilder<I, O> clear() {
        elements.clear();
        return this;
    }

    /**
     * 根据输入，使用所有能成功处理的构建元素来生成一个输出值的集合。
     *
     * @param input 输入值
     * @return 包含所有成功构建的输出值的集合
     */
    public Collection<O> buildAll(I input) {
        return elements.stream()
                .map(element -> element.apply(input))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * 根据输入，查找第一个能成功处理的构建元素并返回其输出。
     * <p>
     * 元素处理的顺序取决于它们的注册顺序。
     *
     * @param input 输入值
     * @return 如果找到能处理的元素，则返回包含输出值的 {@link Optional}；否则返回空 {@link Optional}
     */
    public Optional<O> build(I input) {
        return elements.stream()
                .map(element -> element.apply(input))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    /**
     * 获取所有已注册构建元素的不可变集合。
     *
     * @return 所有构建元素的集合视图
     */
    public Collection<Function<I, Optional<O>>> getElements() {
        return Collections.unmodifiableCollection(elements);
    }
}