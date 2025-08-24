package cn.gloomcore.ui;

import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * �˵������࣬���ڶ���͹���GUI�˵��Ĳ��ֽṹ
 * <p>
 * ����ͨ���ַ�ӳ��ķ�ʽ����˵����֣����ַ�����Ʒ����λ����������
 * �����ڴ����˵�ʱ���ٶ�λ�ͷ�����Ʒ
 */
public class MenuLayout {
    private final int size;
    private final Char2ObjectOpenHashMap<IntArrayList> slots;
    private final InventoryType inventoryType;

    /**
     * ����һ���µĲ˵�����ʵ��
     *
     * @param size          �˵���С
     * @param slots         �ַ�����λ�б��ӳ���ϵ
     * @param inventoryType ��Ʒ������
     */
    protected MenuLayout(int size, Char2ObjectOpenHashMap<IntArrayList> slots, InventoryType inventoryType) {
        this.size = size;
        this.slots = slots;
        this.inventoryType = inventoryType;
    }

    /**
     * �����ṩ�Ĳ����ַ����б���һ���������͵Ĳ˵�����
     * <p>
     * ����ͨ���ַ����б��壬ÿ���ַ�������һ�У����9���ַ��������6�С�
     * �ַ����е�ÿ���ַ�����һ����λ����ͬ�ַ���ʾ��ͬ���͵Ĳ�λ��
     *
     * @param layout �����ַ����б�ÿ���ַ�������˵���һ��
     * @return �´����Ĳ˵�����ʵ��
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
     * ��ȡָ���ַ���Ӧ�����в�λλ��
     *
     * @param key ���ڲ��Ҳ�λ���ַ���
     * @return ��������ƥ���λλ�õ����飬���û��ƥ���򷵻ؿ�����
     */
    public int[] getSlots(char key) {
        IntArrayList list = slots.get(key);
        return (list != null) ? list.toIntArray() : new int[0];
    }

    /**
     * ��ȡ�˵���С
     *
     * @return �˵����ܲ���
     */
    public int getSize() {
        return size;
    }

    /**
     * ��ȡ��Ʒ������
     *
     * @return InventoryTypeö��ֵ����ʾ�˵�������
     */
    public InventoryType getInventoryType() {
        return inventoryType;
    }

}