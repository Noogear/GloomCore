package cn.gloomcore.ui;

import cn.gloomcore.ui.puzzle.Puzzle;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * GUI��ͼ�࣬����һ���ɽ�����GUI����
 * <p>
 * �������GUI�е�����ƴͼ(Puzzle)����������¼�����Ⱦ�������ݡ�
 * ÿ��GUI��ͼ�����ض�������������������һ���˵����ֶ���
 */
public class PuzzleGuiView implements InventoryHolder {
    private final Set<Puzzle> puzzles = new HashSet<>();
    private final Puzzle[] slotPuzzleArray;
    private final UUID playerId;
    private final MenuLayout menuLayout;

    private @Nullable Inventory inventory;

    /**
     * ����һ���µ�GUI��ͼʵ��
     *
     * @param menuLayout �˵����ֶ���
     * @param player     ���������
     */
    public PuzzleGuiView(MenuLayout menuLayout, Player player) {
        this.menuLayout = menuLayout;
        this.slotPuzzleArray = new Puzzle[menuLayout.getSize()];
        this.playerId = player.getUniqueId();
    }

    /**
     * ��GUI��ͼ�����һ��ƴͼ���
     * <p>
     * ƴͼ����ᱻ��ӵ���ͼ��ƴͼ�����У����������λ���䵽��Ӧ�Ĳ�λ�����С�
     * ���ָ����λ�ѱ�ռ�ã����׳��쳣
     *
     * @param puzzle Ҫ��ӵ�ƴͼ���
     * @throws IllegalArgumentException ����λ�ѱ�ռ��ʱ�׳�
     */
    protected void addPuzzle(Puzzle puzzle) {
        this.puzzles.add(puzzle);
        for (Integer slot : puzzle.getSlots()) {
            if (slot >= 0 && slot < this.slotPuzzleArray.length) {
                if (this.slotPuzzleArray[slot] != null) {
                    throw new IllegalArgumentException("Slot " + slot + " is already occupied!");
                }
                this.slotPuzzleArray[slot] = puzzle;
            }
        }
    }

    /**
     * ��Ⱦ����ƴͼ�������ҵĿ����
     *
     * @param player Ŀ�����
     */
    private void renderAll(Player player) {
        if (inventory != null) {
            puzzles.forEach(puzzle -> puzzle.render(player, this.inventory));
        }
    }

    /**
     * ���������¼�
     * <p>
     * ȡ������Ĭ���¼�����Ȼ���¼�ת������Ӧ��λ��ƴͼ�������
     *
     * @param event ������¼�
     */
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        findPuzzleBySlot(event.getRawSlot()).ifPresent(puzzle -> puzzle.onClick(event));
    }

    /**
     * ��������ק�¼�
     * <p>
     * ��ǰʵ��Ϊ�գ�����������д���ṩ���幦��
     *
     * @param event �����ק�¼�
     */
    public void handleDrag(InventoryDragEvent event) {
    }

    /**
     * ��������¼�
     * <p>
     * ��ǰʵ��Ϊ�գ�����������д���ṩ���幦��
     *
     * @param event �����¼�
     */
    public void handleOpen(InventoryOpenEvent event) {
    }

    /**
     * ������ر��¼�
     * <p>
     * ��ǰʵ��Ϊ�գ�����������д���ṩ���幦��
     *
     * @param event ���ر��¼�
     */
    public void handleClose(InventoryCloseEvent event) {
    }

    /**
     * �����˵�����
     *
     * @return �˵��������
     */
    public Component parsedMenuTitle() {
        return menuLayout.getInventoryType().defaultTitle();
    }


    /**
     * ���ݲ�λ���Ҷ�Ӧ��ƴͼ���
     *
     * @param slot ��λ����
     * @return ����ƴͼ�����Optional�������δ�ҵ���Ϊ��
     */
    private Optional<Puzzle> findPuzzleBySlot(int slot) {
        if (slot >= 0 && slot < this.slotPuzzleArray.length) {
            return Optional.ofNullable(this.slotPuzzleArray[slot]);
        }
        return Optional.empty();
    }

    /**
     * Ϊ��Ҵ򿪴�GUI��ͼ
     *
     * @param player Ŀ�����
     */
    public void open(Player player) {
        if (inventory == null) {
            this.inventory = Bukkit.createInventory(this, menuLayout.getSize(), parsedMenuTitle());
        }
        renderAll(player);
        player.openInventory(inventory);
    }

    /**
     * ��ȡ�����ͼ�����Ŀ�����
     *
     * @return ������
     */
    @Override
    public @NotNull Inventory getInventory() {
        if (inventory != null) {
            return inventory;
        }
        return inventory = Bukkit.createInventory(this, menuLayout.getInventoryType());
    }
}
