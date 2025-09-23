package gloomcore.paper.gui.icon;

import gloomcore.paper.contract.Context;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * 图标类，用于表示UI中的图标元素
 * <p>
 * 包含图标的显示内容和点击时的响应动作
 */
public class Icon<C extends Context<Player>> {
    protected Function<C, ItemStack> display;
    protected IconAction<C> iconAction;

    /**
     * 构造函数，创建一个只有显示内容的图标
     *
     * @param display 图标的显示内容
     */
    public Icon(@NotNull Function<C, ItemStack> display) {
        this.display = display;
    }

    /**
     * 拷贝构造函数
     *
     * @param other 要拷贝的源 Icon 对象
     */
    public Icon(@NotNull Icon<C> other) {
        this.display = other.display;
        this.iconAction = other.iconAction;
    }

    /**
     * 构造函数，创建一个包含显示内容和点击动作的图标
     *
     * @param display    图标的显示内容
     * @param iconAction 图标的点击动作
     */
    public Icon(@NotNull Function<C, ItemStack> display, @Nullable IconAction<C> iconAction) {
        this.display = display;
        this.iconAction = iconAction;
    }

    /**
     * 处理图标的点击事件
     *
     * @param event   点击事件
     * @param context 上下文
     * @return 当前图标实例，支持链式调用
     */
    public Icon<C> onClick(InventoryClickEvent event, C context) {
        if (iconAction != null) {
            iconAction.onClick(event, context);
        }
        return this;
    }

    /**
     * 处理指定点击类型的点击事件
     *
     * @param clickType 点击类型
     * @param context   点击上下文
     * @return 当前图标实例，支持链式调用
     */
    public Icon<C> onClick(@NotNull ClickType clickType, @Nullable C context) {
        if (iconAction != null) {
            iconAction.onClick(clickType, context);
        }
        return this;
    }

    /**
     * 设置图标的点击动作
     *
     * @param iconAction 点击动作
     * @return 当前图标实例，支持链式调用
     */
    public Icon<C> setClickAction(@Nullable IconAction<C> iconAction) {
        this.iconAction = iconAction;
        return this;
    }

    /**
     * 设置图标的显示内容
     *
     * @param display 显示内容
     * @return 当前图标实例，支持链式调用
     */
    public Icon<C> setDisplay(@NotNull Function<C, ItemStack> display) {
        this.display = display;
        return this;
    }

    /**
     * 获取图标的显示物品
     *
     * @param context 上下文
     * @return 图标的物品堆实例
     */
    public ItemStack display(C context) {
        return display.apply(context);
    }
}
