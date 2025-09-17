package gloomcore.paper.gui.context;

import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

/**
 * 一个简单的 Context 实现，通过玩家 UUID 标识所有者。
 */
public record DefaultPlayerContext(UUID playerId) implements Context {
    public DefaultPlayerContext(UUID playerId) {
        this.playerId = Objects.requireNonNull(playerId, "playerId");
    }

    public static DefaultPlayerContext of(Player player) {
        return new DefaultPlayerContext(player.getUniqueId());
    }
}

