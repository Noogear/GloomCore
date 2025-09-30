package gloomcore.paper.gui.layout;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 菜单布局类，用于定义和管理GUI菜单的布局结构。
 * <p>
 * 该类通过字符模板来定义菜单的初始布局，将字符与物品栏槽位（slot）关联起来，
 * 并提供了通过坐标直接检索初始字符的功能。同时，它允许在运行时动态修改字符与槽位的映射关系。
 */
public class ChestLayout implements GuiLayout {

    private final int width;
    private final int height;
    private final int size;
    private final char[][] layoutGrid; // 代表初始的、不可变的布局结构
    private final Char2ObjectMap<IntArrayList> slotsMap; // 动态的、可变的槽位映射

    /**
     * 根据提供的布局字符串列表创建一个箱子类型的菜单布局。
     * <p>
     * 布局通过字符串列表定义，每个字符串代表一行。
     * 字符串中的每个字符代表一个槽位，相同字符表示相同类型的槽位。
     * 布局的宽度固定为9，高度最多为6行。
     *
     * @param layoutTemplate 布局字符串列表，每个字符串代表菜单的一行。
     */
    public ChestLayout(@NotNull List<String> layoutTemplate) {
        this.height = Math.min(layoutTemplate.size(), 6);
        this.width = 9;
        this.size = this.height * this.width;
        this.layoutGrid = new char[height][width];
        this.slotsMap = new Char2ObjectOpenHashMap<>();

        for (int row = 0; row < height; row++) {
            String line = layoutTemplate.get(row);
            for (int col = 0; col < width; col++) {
                // 如果当前行的字符串长度不足9，则用空字符' '填充剩余部分
                char key = (col < line.length()) ? line.charAt(col) : ' ';
                this.layoutGrid[row][col] = key;
                if (key != ' ') {
                    int slotIndex = row * width + col;
                    slotsMap.computeIfAbsent(key, k -> new IntArrayList()).add(slotIndex);
                }
            }
        }
    }

    /**
     * 复制构造函数。
     * <p>
     * 创建一个与提供的 ChestLayout 对象状态完全独立的深拷贝新实例。
     * 对新实例的槽位映射 (slotsMap) 进行的任何修改都不会影响原始对象。
     *
     * @param other 要复制的 ChestLayout 对象。
     */
    public ChestLayout(@NotNull ChestLayout other) {
        this.width = other.width;
        this.height = other.height;
        this.size = other.size;

        this.layoutGrid = new char[other.height][];
        for (int i = 0; i < other.height; i++) {
            this.layoutGrid[i] = other.layoutGrid[i].clone();
        }
        this.slotsMap = new Char2ObjectOpenHashMap<>(other.slotsMap.size());
        for (Char2ObjectMap.Entry<IntArrayList> entry : other.slotsMap.char2ObjectEntrySet()) {
            this.slotsMap.put(entry.getCharKey(), new IntArrayList(entry.getValue()));
        }
    }

    /**
     * 获取指定字符对应的所有槽位索引。
     * 返回的是当前动态映射的结果。
     *
     * @param key 用于查找槽位的字符键。
     * @return 包含所有匹配槽位索引的数组，如果字符不存在则返回空数组。
     */
    public int[] getSlots(char key) {
        IntArrayList list = slotsMap.get(key);
        return (list != null) ? list.toIntArray() : new int[0];
    }

    /**
     * 根据坐标（行和列）获取初始布局中对应位置的字符。
     * 注意：此方法返回的是构造时的初始字符，不会反映后续对slotsMap的修改。
     *
     * @param row 行号 (从0开始)。
     * @param col 列号 (从0开始)。
     * @return 对应坐标的初始字符。
     * @throws IndexOutOfBoundsException 如果坐标超出布局范围。
     */
    public char getChar(int row, int col) {
        if (row < 0 || row >= height || col < 0 || col >= width) {
            throw new IndexOutOfBoundsException("Coordinates out of bounds: (" + row + ", " + col + ")");
        }
        return layoutGrid[row][col];
    }

    /**
     * 返回布局的二维网格视图。
     * 返回的是初始布局，是不可变的。
     *
     * @return char[][] 布局网格的副本，防止外部修改。
     */
    public char[][] getLayoutGrid() {
        char[][] copy = new char[height][];
        for (int i = 0; i < height; i++) {
            copy[i] = layoutGrid[i].clone();
        }
        return copy;
    }

    /**
     * 为指定字符添加一个新的槽位。如果该字符已包含此槽位，则不进行任何操作。
     *
     * @param key  目标字符。
     * @param slot 要添加的槽位索引。
     */
    public void addSlot(char key, int slot) {
        if (key == ' ') return; // 不允许为空格字符分配槽位
        IntArrayList slots = this.slotsMap.computeIfAbsent(key, k -> new IntArrayList());
        if (!slots.contains(slot)) {
            slots.add(slot);
        }
    }

    /**
     * 移除指定字符绑定的一个槽位。
     *
     * @param key  目标字符。
     * @param slot 要移除的槽位索引。
     * @return 如果成功移除了槽位，则返回 true。
     */
    public boolean removeSlot(char key, int slot) {
        IntArrayList slots = this.slotsMap.get(key);
        if (slots != null) {
            boolean removed = slots.rem(slot); // rem(value) 用于按值移除
            if (slots.isEmpty()) {
                this.slotsMap.remove(key); // 如果列表为空，则从Map中移除该键
            }
            return removed;
        }
        return false;
    }

    /**
     * 移除指定字符及其绑定的所有槽位。
     *
     * @param key 要移除的字符键。
     */
    public void clearSlots(char key) {
        this.slotsMap.remove(key);
    }

    /**
     * 清空所有字符与槽位的绑定关系。
     */
    public void clearAllSlots() {
        this.slotsMap.clear();
    }

    /**
     * 强制设置或替换某个字符的所有槽位。
     *
     * @param key   目标字符。
     * @param slots 新的槽位索引数组。如果为空，则会移除该字符。
     */
    public void setSlots(char key, int... slots) {
        if (key == ' ') return;
        if (slots == null || slots.length == 0) {
            this.slotsMap.remove(key);
        } else {
            this.slotsMap.put(key, new IntArrayList(slots));
        }
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return this.height;
    }

    @Override
    public InventoryType inventoryType() {
        return InventoryType.CHEST;
    }

    @Override
    public int size() {
        return size;
    }
}