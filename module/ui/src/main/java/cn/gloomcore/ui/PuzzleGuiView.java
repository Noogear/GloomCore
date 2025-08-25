package cn.gloomcore.ui;

import cn.gloomcore.ui.puzzle.PlaceablePuzzle;
import cn.gloomcore.ui.puzzle.Puzzle;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * GUI��ͼ�࣬����һ���ɽ�����GUI����
 * <p>
 * �������GUI�е�����ƴͼ(Puzzle)����������¼�����Ⱦ�������ݡ�
 * ÿ��GUI��ͼ�����ض�������������������һ���˵����ֶ���
 */
public class PuzzleGuiView implements InventoryHolder {
    private final List<Puzzle> puzzles = new ArrayList<>();
    private final List<PlaceablePuzzle> placeablePuzzles = new ArrayList<>();
    private final Puzzle[] slotPuzzleArray;
    private final MenuLayout menuLayout;

    private @Nullable Inventory inventory;

    /**
     * ����һ���µ�GUI��ͼʵ��
     *
     * @param menuLayout �˵����ֶ���
     */
    public PuzzleGuiView(MenuLayout menuLayout) {
        this.menuLayout = menuLayout;
        this.slotPuzzleArray = new Puzzle[menuLayout.getSize()];
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
        if (puzzle instanceof PlaceablePuzzle placeablePuzzle) {
            this.placeablePuzzles.add(placeablePuzzle);
        }

    }

    /**
     * ��Ⱦ����ƴͼ�������ҵĿ����
     *
     * @param player Ŀ�����
     */
    private void renderAll(Player player) {
        puzzles.forEach(puzzle -> puzzle.render(player, getInventory()));
    }


    public void handleClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (this.getInventory().equals(clickedInventory)) {
            event.setCancelled(true);
            findPuzzleBySlot(event.getRawSlot()).ifPresent(puzzle -> puzzle.onClick(event));
            return;
        }

        if (clickedInventory != null) {
            InventoryAction action = event.getAction();
            if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                event.setCancelled(true);
                ItemStack itemToMove = event.getCurrentItem();
                if (itemToMove == null || itemToMove.isEmpty()) {
                    return;
                }
                for (PlaceablePuzzle puzzle : this.placeablePuzzles) {
                    puzzle.tryAcceptItem(itemToMove, this.getInventory());
                    if (itemToMove.getAmount() <= 0) {
                        event.setCurrentItem(null);
                        break;
                    }
                }

            } else if (action == InventoryAction.COLLECT_TO_CURSOR) {
                handleCollectToCursor(event);
            }
            // �������������ڲ��� (��PICKUP_ALL, SWAP_WITH_CURSOR)�����ǲ����棬����ԭ����Ϊ
        }
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


    public void handleClose(Player player) {
        if (placeablePuzzles.isEmpty()) {
            return;
        }
        for (PlaceablePuzzle placeablePuzzle : placeablePuzzles) {
            placeablePuzzle.cleanupOnClose(player, this.getInventory());
        }
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
        renderAll(player);
        player.openInventory(this.getInventory());
    }

    /**
     * ��ȡ�����ͼ�����Ŀ�����
     *
     * @return ������
     */
    @Override
    public @NotNull Inventory getInventory() {
        if (inventory == null) {
            return inventory = Bukkit.createInventory(this, menuLayout.getInventoryType());
        }
        return inventory;
    }
}
