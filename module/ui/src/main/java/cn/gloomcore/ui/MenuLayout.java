package cn.gloomcore.ui;

import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 菜单布局类，用于定义和管理GUI菜单的布局结构
 * <p>
 * 该类通过字符映射的方式定义菜单布局，将字符与物品栏槽位关联起来，
 * 便于在创建菜单时快速定位和放置物品
 */
public class MenuLayout {
    private final int size;
    private final Char2ObjectOpenHashMap<IntArrayList> slots;
    private final InventoryType inventoryType;

    /**
     * 构造一个新的菜单布局实例
     *
     * @param size          菜单大小
     * @param slots         字符到槽位列表的映射关系
     * @param inventoryType 物品栏类型
     */
    protected MenuLayout(int size, Char2ObjectOpenHashMap<IntArrayList> slots, InventoryType inventoryType) {
        this.size = size;
        this.slots = slots;
        this.inventoryType = inventoryType;
    }

    /**
     * 根据提供的布局字符串列表创建一个箱子类型的菜单布局
     * <p>
     * 布局通过字符串列表定义，每个字符串代表一行（最大9个字符），最多6行。
     * 字符串中的每个字符代表一个槽位，相同字符表示相同类型的槽位。
     *
     * @param layout 布局字符串列表，每个字符串代表菜单的一行
     * @return 新创建的菜单布局实例
     */
    public static MenuLayout ofChest(@NotNull List<String> layout) {
        int maxRows = Math.min(layout.size(), 6);
        Char2ObjectOpenHashMap<IntArrayList> slots = new Char2ObjectOpenHashMap<>();
        for (int x = 0; x < maxRows; x++) {
            String line = layout.get(x);
            if (line == null || line.isEmpty()) continue;
            for (int y = 0; y < Math.min(line.length(), 9); y++) {
                char key = line.charAt(y);
                int slot = x * 9 + y;
                slots.computeIfAbsent(key, k -> new IntArrayList()).add(slot);
            }
        }
        return new MenuLayout(maxRows * 9, slots, InventoryType.CHEST);
    }


    /**
     * 获取指定字符对应的所有槽位位置
     *
     * @param key 用于查找槽位的字符键
     * @return 包含所有匹配槽位位置的数组，如果没有匹配则返回空数组
     */
    public int[] getSlots(char key) {
        IntArrayList list = slots.get(key);
        return (list != null) ? list.toIntArray() : new int[0];
    }

    /**
     * 获取菜单大小
     *
     * @return 菜单的总槽数
     */
    public int getSize() {
        return size;
    }

    /**
     * 获取物品栏类型
     *
     * @return InventoryType枚举值，表示菜单的类型
     */
    public InventoryType getInventoryType() {
        return inventoryType;
    }

}