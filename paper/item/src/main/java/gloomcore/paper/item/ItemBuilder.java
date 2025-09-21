package gloomcore.paper.item;

import gloomcore.paper.contract.builder.SelfBuilder;
import io.papermc.paper.datacomponent.DataComponentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NullMarked;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@NullMarked
public class ItemBuilder implements SelfBuilder<ItemBuilder, ItemStack> {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final ItemStack itemStack;
    private final ItemMeta meta;

    private ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        this.meta = this.itemStack.getItemMeta();
    }

    private ItemBuilder(ItemBuilder other) {
        this.itemStack = other.itemStack.clone();
        this.meta = other.meta.clone();
    }

    /**
     * 创建指定材料的物品构建器
     *
     * @param material 物品材料
     * @return ItemBuilder实例
     */
    public static ItemBuilder of(Material material) {
        return new ItemBuilder(new ItemStack(material));
    }

    /**
     * 创建指定材料和数量的物品构建器
     *
     * @param material 物品材料
     * @param amount   物品数量
     * @return ItemBuilder实例
     */
    public static ItemBuilder of(Material material, int amount) {
        return new ItemBuilder(new ItemStack(material, amount));
    }

    /**
     * 基于现有的ItemStack创建物品构建器
     *
     * @param itemStack 现有的ItemStack
     * @return ItemBuilder实例
     */
    public static ItemBuilder of(ItemStack itemStack) {
        return new ItemBuilder(itemStack);
    }

    /**
     * 从另一个ItemBuilder创建新的ItemBuilder
     *
     * @param other 另一个ItemBuilder实例
     * @return 新的ItemBuilder实例
     */
    public static ItemBuilder from(ItemBuilder other) {
        return new ItemBuilder(other.build());
    }

    /**
     * 构建最终的ItemStack对象
     *
     * @return 构建完成的ItemStack对象
     */
    @Override
    public ItemStack build() {
        this.itemStack.setItemMeta(this.meta);
        return this.itemStack.clone();
    }

    /**
     * 配置当前构建器实例
     *
     * @param builderConsumer 用于配置构建器的消费者函数
     * @return 当前ItemBuilder实例
     */
    @Override
    public ItemBuilder configure(Consumer<ItemBuilder> builderConsumer) {
        builderConsumer.accept(this);
        return this;
    }

    /**
     * 创建当前构建器的一个精确副本。
     * 这是一个深拷贝，因为它基于 build() 方法创建的克隆 ItemStack。
     *
     * @return 一个新的 ItemBuilder 实例，状态与当前实例完全相同。
     */
    @Override
    public ItemBuilder copy() {
        return new ItemBuilder(this.build());
    }

    /**
     * 对特定类型的ItemMeta执行操作
     *
     * @param metaClass ItemMeta的类型类
     * @param consumer  对ItemMeta执行的操作
     * @param <T>       ItemMeta的具体类型
     * @return 当前ItemBuilder实例
     */
    public <T extends ItemMeta> ItemBuilder meta(Class<T> metaClass, Consumer<T> consumer) {
        if (metaClass.isInstance(this.meta)) {
            consumer.accept(metaClass.cast(this.meta));
        }
        return this;
    }

    /**
     * 对ItemMeta执行操作
     *
     * @param consumer 对ItemMeta执行的操作
     * @return 当前ItemBuilder实例
     */
    public ItemBuilder meta(Consumer<ItemMeta> consumer) {
        consumer.accept(this.meta);
        return this;
    }

    /**
     * 获取物品的指定数据组件值
     *
     * @param type 数据组件类型
     * @param <T>  数据组件值的类型
     * @return 数据组件的值，如果不存在则返回null
     */
    public <T> @Nullable T data(DataComponentType.Valued<T> type) {
        return itemStack.getData(type);
    }

    /**
     * 获取物品的指定数据组件值（包装在Optional中）
     *
     * @param type 数据组件类型
     * @param <T>  数据组件值的类型
     * @return 包含数据组件值的Optional对象
     */
    public <T> Optional<T> optionalData(DataComponentType.Valued<T> type) {
        return Optional.ofNullable(itemStack.getData(type));
    }

    /**
     * 设置物品的指定数据组件值
     *
     * @param type  数据组件类型
     * @param value 要设置的值
     * @param <T>   数据组件值的类型
     * @return 当前ItemBuilder实例
     */
    public <T> ItemBuilder data(DataComponentType.Valued<T> type, T value) {
        itemStack.setData(type, value);
        return this;
    }

    /**
     * 设置物品的无值数据组件
     *
     * @param type 无值数据组件类型
     * @return 当前ItemBuilder实例
     */
    public ItemBuilder data(DataComponentType.NonValued type) {
        itemStack.setData(type);
        return this;
    }

    /**
     * 重置物品的指定数据组件
     *
     * @param type 数据组件类型
     * @return 当前ItemBuilder实例
     */
    public ItemBuilder resetData(DataComponentType type) {
        itemStack.resetData(type);
        return this;
    }

    /**
     * 取消设置物品的指定数据组件
     *
     * @param type 数据组件类型
     * @return 当前ItemBuilder实例
     */
    public ItemBuilder unsetData(DataComponentType type) {
        itemStack.unsetData(type);
        return this;
    }

    /**
     * 设置物品数量
     *
     * @param amount 物品数量
     * @return 当前ItemBuilder实例
     */
    public ItemBuilder amount(int amount) {
        this.itemStack.setAmount(amount);
        return this;
    }

    /**
     * 设置物品显示名称
     *
     * @param name 物品显示名称
     * @return 当前ItemBuilder实例
     */
    public ItemBuilder name(Component name) {
        this.meta.displayName(name.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        return this;
    }

    /**
     * 设置物品显示名称（使用MiniMessage格式）
     *
     * @param miniMessageName 使用MiniMessage格式的名称字符串
     * @return 当前ItemBuilder实例
     */
    public ItemBuilder name(String miniMessageName) {
        return name(MINI_MESSAGE.deserialize(miniMessageName));
    }

    /**
     * 设置物品描述信息
     *
     * @param lines 描述信息行列表
     * @return 当前ItemBuilder实例
     */
    public ItemBuilder lore(List<? extends ComponentLike> lines) {
        List<Component> processedLines = lines.stream()
                .map(ComponentLike::asComponent)
                .map(line -> line.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .toList();
        this.meta.lore(processedLines);
        return this;
    }

    /**
     * 设置物品描述信息
     *
     * @param lines 描述信息行数组
     * @return 当前ItemBuilder实例
     */
    public ItemBuilder lore(ComponentLike... lines) {
        return lore(Arrays.asList(lines));
    }

    /**
     * 设置物品描述信息（使用MiniMessage格式）
     *
     * @param miniMessages 使用MiniMessage格式的描述信息字符串数组
     * @return 当前ItemBuilder实例
     */
    public ItemBuilder lore(String... miniMessages) {
        List<Component> components = Arrays.stream(miniMessages)
                .map(MINI_MESSAGE::deserialize)
                .toList();
        return lore(components);
    }

    /**
     * 向物品现有描述信息追加新行
     *
     * @param lines 要追加的描述信息行列表
     * @return 当前ItemBuilder实例
     */
    public ItemBuilder appendLore(List<? extends ComponentLike> lines) {
        List<Component> currentLore = this.meta.lore();
        if (currentLore == null) {
            currentLore = new ArrayList<>();
        }
        lines.stream()
                .map(ComponentLike::asComponent)
                .map(line -> line.decoration(TextDecoration.ITALIC, false))
                .forEach(currentLore::add);
        this.meta.lore(currentLore);
        return this;
    }

}
