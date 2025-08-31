package gloomcore.contract.template;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 通用模板接口
 *
 * @param <R> 最终生成的产品类型
 * @param <C> 应用模板时所需的上下文类型
 */
public interface ITemplate<C, R> {

    /**
     * 同步应用模板以生成产品
     *
     * @param context 上下文参数
     * @return 生成的产品
     */
    R apply(C context);

    /**
     * 异步应用模板以生成产品
     *
     * @param context 上下文参数
     * @return 一个包含最终产品的 CompletableFuture
     */
    CompletableFuture<R> applyAsync(C context);

    default List<R> applyAll(Collection<C> contexts) {
        return contexts.stream()
                .map(this::apply)
                .collect(Collectors.toList());
    }

}
