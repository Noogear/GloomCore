package gloomcore.paper.item;

import gloomcore.contract.template.ITemplate;
import gloomcore.paper.scheduler.PaperScheduler;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * ItemTemplate类用于创建可参数化的物品模板
 *
 * @param <C> 上下文类型，用于延迟操作的参数
 */

public class ItemTemplate<C> implements ITemplate<C, ItemStack> {

    private final ItemBuilder prebuiltBuilder;
    private final List<BiConsumer<ItemBuilder, C>> delayedActions;

    private ItemTemplate(ItemBuilder prebuiltBuilder, List<BiConsumer<ItemBuilder, C>> delayedActions) {
        this.prebuiltBuilder = prebuiltBuilder;
        this.delayedActions = List.copyOf(delayedActions);
    }

    /**
     * 创建ItemTemplate构建器
     *
     * @param builderSupplier ItemBuilder供应器
     * @param <C>             上下文类型
     * @return ItemTemplate构建器实例
     */
    public static <C> Builder<C> builder(Supplier<ItemBuilder> builderSupplier) {
        return new Builder<>(builderSupplier.get());
    }

    /**
     * 应用模板并构建物品
     *
     * @param context 上下文参数，用于执行延迟操作
     * @return 构建完成的ItemStack物品
     */
    @Override
    public ItemStack apply(C context) {
        return build(context);
    }

    /**
     * 异步应用模板并构建物品
     *
     * @param context 上下文参数，用于执行延迟操作
     * @return 包含构建完成物品的CompletableFuture
     */
    @Override
    public CompletableFuture<ItemStack> applyAsync(C context) {
        return CompletableFuture.supplyAsync(() -> build(context), PaperScheduler.INSTANCE.async().executor());
    }

    /**
     * 使用指定上下文构建物品
     *
     * @param context 上下文参数
     * @return 构建完成的ItemStack物品
     */
    private ItemStack build(C context) {
        ItemBuilder workingBuilder = this.prebuiltBuilder.copy();
        applyToBuilder(workingBuilder, context);
        return workingBuilder.build();
    }

    /**
     * 将此模板的延迟操作应用到指定的 ItemBuilder 实例上。
     * 这个方法不属于 ITemplate 接口，是 ItemTemplate 的特有功能，用于实现模板组合。
     *
     * @param builder 要应用模板的构建器
     * @param context 上下文参数
     */
    public void applyToBuilder(ItemBuilder builder, C context) {
        for (BiConsumer<ItemBuilder, C> action : delayedActions) {
            action.accept(builder, context);
        }
    }

    public static class Builder<C> {
        private final ItemBuilder builder;
        private final List<BiConsumer<ItemBuilder, C>> delayedActions = new ArrayList<>();

        private Builder(ItemBuilder builder) {
            this.builder = builder;
        }

        /**
         * 立即执行操作，直接应用到构建器上
         *
         * @param action 要执行的操作
         * @return 当前构建器实例
         */
        public Builder<C> immediate(Consumer<ItemBuilder> action) {
            action.accept(this.builder);
            return this;
        }

        /**
         * 延迟执行操作，在模板应用时使用上下文参数执行
         *
         * @param action 要执行的延迟操作
         * @return 当前构建器实例
         */
        public Builder<C> delayed(BiConsumer<ItemBuilder, C> action) {
            this.delayedActions.add(action);
            return this;
        }

        /**
         * 构建ItemTemplate实例
         *
         * @return 构建完成的ItemTemplate实例
         */
        public ItemTemplate<C> build() {
            return new ItemTemplate<>(this.builder, this.delayedActions);
        }
    }
}
