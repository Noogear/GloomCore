package cn.gloomcore.action;

import cn.gloomcore.replacer.StringReplacer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Context {
    protected final UUID uuid;
    protected Player player;
    protected Map<String, String> variables;
    protected StringReplacer replacer;

    public Context(UUID uuid) {
        this.uuid = uuid;
    }

    public Context(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();
    }

    @Nullable
    public Player getPlayer() {
        if (player == null) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                player = p;
            }
        }
        return player;
    }

    public void setVariable(@NotNull String key, @Nullable String value) {
        if (variables == null) {
            variables = new HashMap<>();
        }
        variables.put(key, value);
    }

    @Nullable
    public String getVariable(String key) {
        if (variables == null) {
            return null;
        }
        return variables.get(key);
    }

}
