package cn.gloomcore.paper.item;

import cn.gloomcore.paper.scheduler.PaperScheduler;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * ItemTemplate类用于创建可参数化的物品模板
 * 支持立即执行的操作和延迟执行的操作，可以根据上下文动态生成物品
 *
 * @param <C> 上下文类型，用于延迟操作的参数
 */
public class ItemTemplate<C> {
    private final ItemBuilder prebuiltBuilder;
    private final List<BiConsumer<ItemBuilder, C>> delayedActions;

    /**
     * 构造一个新的ItemTemplate实例
     *
     * @param prebuiltBuilder 预构建的ItemBuilder
     * @param delayedActions  延迟执行的操作列表
     */
    private ItemTemplate(ItemBuilder prebuiltBuilder, List<BiConsumer<ItemBuilder, C>> delayedActions) {
        this.prebuiltBuilder = prebuiltBuilder;
        this.delayedActions = List.copyOf(delayedActions);
    }

    /**
     * 创建一个基于材料的ItemTemplate构建器
     *
     * @param material 物品材料
     * @param <C>      上下文类型
     * @return ItemTemplate构建器
     */
    public static <C> Builder<C> builder(Material material) {
        return new Builder<>(ItemBuilder.of(material));
    }

    /**
     * 创建一个基于现有物品的ItemTemplate构建器
     *
     * @param itemStack 基础物品
     * @param <C>       上下文类型
     * @return ItemTemplate构建器
     */
    public static <C> Builder<C> builder(ItemStack itemStack) {
        return new Builder<>(ItemBuilder.of(itemStack));
    }

    /**
     * 应用模板生成物品
     *
     * @param context 上下文参数
     * @return 生成的物品
     */
    public ItemStack apply(C context) {
        return buildItem(context);
    }

    /**
     * 异步应用模板生成物品
     *
     * @param context 上下文参数
     * @return CompletableFuture<ItemStack> 异步生成的物品
     */
    public CompletableFuture<ItemStack> applyAsync(C context) {
        return CompletableFuture.supplyAsync(() -> buildItem(context), PaperScheduler.INSTANCE.async().executor());
    }

    /**
     * 根据上下文构建物品
     *
     * @param context 上下文参数
     * @return 构建的物品
     */
    private ItemStack buildItem(C context) {
        ItemBuilder workingBuilder = ItemBuilder.copyOf(prebuiltBuilder);
        for (BiConsumer<ItemBuilder, C> action : delayedActions) {
            action.accept(workingBuilder, context);
        }
        return workingBuilder.build();
    }

    /**
     * ItemTemplate构建器类
     * 用于构建ItemTemplate实例，支持立即执行和延迟执行的操作
     *
     * @param <C> 上下文类型
     */
    public static class Builder<C> {
        private final ItemBuilder itemBuilder;
        private final List<Consumer<ItemBuilder>> immediateActions = new ArrayList<>();
        private final List<BiConsumer<ItemBuilder, C>> delayedActions = new ArrayList<>();
        private boolean delayedPhaseStarted = false;

        /**
         * 构造一个新的构建器实例
         *
         * @param itemBuilder 基础ItemBuilder
         */
        private Builder(ItemBuilder itemBuilder) {
            this.itemBuilder = itemBuilder;
        }

        /**
         * 添加立即执行的操作
         * 必须在添加延迟操作之前调用
         *
         * @param action 要执行的操作
         * @return 构建器实例
         * @throws IllegalStateException 如果已经添加了延迟操作
         */
        public Builder<C> immediate(Consumer<ItemBuilder> action) {
            if (delayedPhaseStarted) {
                throw new IllegalStateException("Cannot add immediate actions after delayed actions have been added");
            }
            immediateActions.add(action);
            return this;
        }

        /**
         * 添加延迟执行的操作
         * 调用此方法后不能再添加立即执行的操作
         *
         * @param action 要执行的操作
         * @return 构建器实例
         */
        public Builder<C> delayed(BiConsumer<ItemBuilder, C> action) {
            delayedPhaseStarted = true;
            delayedActions.add(action);
            return this;
        }

        /**
         * 构建ItemTemplate实例
         *
         * @return ItemTemplate实例
         */
        public ItemTemplate<C> build() {
            for (Consumer<ItemBuilder> action : immediateActions) {
                action.accept(this.itemBuilder);
            }
            return new ItemTemplate<>(this.itemBuilder, this.delayedActions);
        }
    }

}
