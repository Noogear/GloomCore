package cn.gloomcore.paper.gui.icon;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 图标类，用于表示UI中的图标元素
 * <p>
 * 包含图标的显示内容和点击时的响应动作
 */
public class Icon {
    protected IconDisplay display;
    protected IconAction iconAction;

    /**
     * 构造函数，创建一个只有显示内容的图标
     *
     * @param display 图标的显示内容
     */
    public Icon(@NotNull IconDisplay display) {
        this.display = display;
    }

    /**
     * 拷贝构造函数
     *
     * @param other 要拷贝的源 Icon 对象
     */
    public Icon(@NotNull Icon other) {
        this.display = other.display;
        this.iconAction = other.iconAction;
    }

    /**
     * 构造函数，创建一个包含显示内容和点击动作的图标
     *
     * @param display    图标的显示内容
     * @param iconAction 图标的点击动作
     */
    public Icon(@NotNull IconDisplay display, @Nullable IconAction iconAction) {
        this.display = display;
        this.iconAction = iconAction;
    }

    /**
     * 处理图标的点击事件
     *
     * @param event 点击事件
     * @return 当前图标实例，支持链式调用
     */
    public Icon onClick(InventoryClickEvent event) {
        if (iconAction != null) {
            iconAction.onClick(event);
        }
        return this;
    }

    /**
     * 处理指定点击类型的点击事件
     *
     * @param clickType 点击类型
     * @param player    点击的玩家
     * @return 当前图标实例，支持链式调用
     */
    public Icon onClick(@NotNull ClickType clickType, @Nullable Player player) {
        if (iconAction != null) {
            iconAction.onClick(clickType, player);
        }
        return this;
    }

    /**
     * 设置图标的点击动作
     *
     * @param iconAction 点击动作
     * @return 当前图标实例，支持链式调用
     */
    public Icon setClickAction(@Nullable IconAction iconAction) {
        this.iconAction = iconAction;
        return this;
    }

    /**
     * 设置图标的显示内容
     *
     * @param display 显示内容
     * @return 当前图标实例，支持链式调用
     */
    public Icon setDisplay(@NotNull IconDisplay display) {
        this.display = display;
        return this;
    }

    /**
     * 获取图标的显示物品
     *
     * @return 图标的物品堆实例
     */
    public ItemStack display() {
        return display.parse();
    }

    public ItemStack display(Player player) {
        return display.parse(player);
    }


}
