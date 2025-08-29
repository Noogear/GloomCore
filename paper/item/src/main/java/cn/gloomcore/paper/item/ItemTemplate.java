package cn.gloomcore.paper.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ItemTemplate<C> {
    private final ItemBuilder prebuiltBuilder;
    private final List<BiConsumer<ItemBuilder, C>> delayedActions;

    private ItemTemplate(ItemBuilder prebuiltBuilder, List<BiConsumer<ItemBuilder, C>> delayedActions) {
        this.prebuiltBuilder = prebuiltBuilder;
        this.delayedActions = List.copyOf(delayedActions);
    }

    public ItemStack apply(C context) {
        ItemBuilder workingBuilder = ItemBuilder.copyOf(prebuiltBuilder);
        for (BiConsumer<ItemBuilder, C> action : delayedActions) {
            action.accept(workingBuilder, context);
        }
        return workingBuilder.build();
    }

    public CompletableFuture<ItemStack> applyAsync(C context) {
        return CompletableFuture.supplyAsync(() -> {
            // 这部分代码将在 ForkJoinPool 的一个工作线程中执行
            ItemBuilder workingBuilder = ItemBuilder.copyOf(prebuiltBuilder);
            for (BiConsumer<ItemBuilder, C> action : delayedActions) {
                action.accept(workingBuilder, context);
            }
            return workingBuilder.build();
        });
    }

    public static <C> Builder<C> builder(Material material) {
        return new Builder<>(ItemBuilder.of(material));
    }

    public static <C> Builder<C> builder(ItemStack itemStack) {
        return new Builder<>(ItemBuilder.of(itemStack));
    }

    public static class Builder<C> {
        private final ItemBuilder itemBuilder;
        private final List<Consumer<ItemBuilder>> immediateActions = new ArrayList<>();
        private final List<BiConsumer<ItemBuilder, C>> delayedActions = new ArrayList<>();
        private boolean delayedPhaseStarted = false;

        private Builder(ItemBuilder itemBuilder) {
            this.itemBuilder = itemBuilder;
        }

        public Builder<C> immediate(Consumer<ItemBuilder> action) {
            if (delayedPhaseStarted) {
                throw new IllegalStateException("不能在添加延时(delayed)操作后，再添加立刻(immediate)操作。");
            }
            immediateActions.add(action);
            return this;
        }

        public Builder<C> delayed(BiConsumer<ItemBuilder, C> action) {
            delayedPhaseStarted = true;
            delayedActions.add(action);
            return this;
        }

        public ItemTemplate<C> build() {
            // 1. 在模板创建时，执行所有“立刻”操作。
            for (Consumer<ItemBuilder> action : immediateActions) {
                action.accept(this.itemBuilder);
            }
            // 2. 创建并返回模板实例，其中包含了预处理过的 ItemBuilder 和所有“延时”操作。
            return new ItemTemplate<>(this.itemBuilder, this.delayedActions);
        }
        }

}
