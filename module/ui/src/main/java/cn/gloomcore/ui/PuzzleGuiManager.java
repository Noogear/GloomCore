package cn.gloomcore.ui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GUI�������࣬���ڹ�����ҵ�GUI���浼������ʷ��¼
 * <p>
 * ���ฺ������Ҵ�GUI��������һ��GUI���Լ�����������ݵȲ�����
 * ͬʱ��������¼���ת������Ӧ��GUI��ͼ����
 */
public class PuzzleGuiManager implements Listener {
    private final ConcurrentHashMap<UUID, Deque<PuzzleGuiView>> history = new ConcurrentHashMap<>();
    private final Set<UUID> navigatingPlayers = ConcurrentHashMap.newKeySet();

    /**
     * ����һ���µ�GUI������ʵ������ע���¼�������
     *
     * @param plugin ���ʵ��������ע���¼�������
     */
    public PuzzleGuiManager(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    /**
     * Ϊ��Ҵ�һ���µ�GUI��������ǰGUI������У�������ʷ��¼��
     *
     * @param player       ���
     * @param view         Ҫ�򿪵�GUI
     * @param storeHistory �Ƿ񽫵�ǰ�򿪵�GUI������ʷ
     */
    public void open(Player player, PuzzleGuiView view, boolean storeHistory) {
        if (storeHistory && player.getOpenInventory().getTopInventory().getHolder(false) instanceof PuzzleGuiView currentHolder) {
            history.computeIfAbsent(player.getUniqueId(), k -> new ArrayDeque<>()).push(currentHolder);
        }
        navigatingPlayers.add(player.getUniqueId());
        view.open(player);
    }

    /**
     * Ϊ��Ҵ�һ����GUI��Ĭ�ϲ��洢��ʷ�������ڴ����˵��ȳ�������
     *
     * @param player ���
     * @param view   Ҫ�򿪵�GUI��ͼ
     */
    public void open(Player player, PuzzleGuiView view) {
        history.remove(player.getUniqueId());
        navigatingPlayers.add(player.getUniqueId());
        view.open(player);
    }

    /**
     * ������һҳ��
     *
     * @param player ���
     * @return ����ɹ����أ�����true
     */
    public boolean back(Player player) {
        Deque<PuzzleGuiView> playerHistory = history.get(player.getUniqueId());
        if (playerHistory != null && !playerHistory.isEmpty()) {
            PuzzleGuiView previousView = playerHistory.pop();
            previousView.open(player);
            return true;
        }
        return false;
    }

    /**
     * ���������¼������¼�ת������Ӧ��GUI��ͼ���д���
     *
     * @param event ������¼�
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder(false) instanceof PuzzleGuiView puzzleGuiView) {
            puzzleGuiView.handleClick(event);
        }
    }

    /**
     * ��������˳��¼����������ҵ���ʷ��¼�͵���״̬
     *
     * @param event ����˳��¼�
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUuid = event.getPlayer().getUniqueId();
        history.remove(playerUuid);
        navigatingPlayers.remove(playerUuid);
    }

    /**
     * ������ر��¼������ݹر�ԭ������Ƿ�������ҵ���ʷ��¼
     *
     * @param event ���ر��¼�
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder(false) instanceof PuzzleGuiView) {
            UUID playerUuid = event.getPlayer().getUniqueId();
            if (navigatingPlayers.remove(playerUuid)) {
                return;
            }
            history.remove(playerUuid);
        }
    }


}
