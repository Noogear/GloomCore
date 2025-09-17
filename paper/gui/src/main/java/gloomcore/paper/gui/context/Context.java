package gloomcore.paper.gui.context;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 通用 GUI 上下文接口，通过玩家 UUID 标识语义上的“所有者”。
 * 提供便捷方法解析在线玩家实例（若在线）。
 */
public interface Context {
    /**
     * 拥有者玩家的 UUID。
     */
    UUID playerId();

    /**
     * 解析为在线 Player（若玩家在线则返回实例，否则返回 null）。
     */
    default @Nullable Player player() {
        return Bukkit.getPlayer(playerId());
    }
}

