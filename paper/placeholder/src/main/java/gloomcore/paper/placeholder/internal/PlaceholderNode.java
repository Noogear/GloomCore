package gloomcore.paper.placeholder.internal;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderNode {
    private final Object2ObjectOpenHashMap<String, PlaceholderNode> children = new Object2ObjectOpenHashMap<>();
    private ParmPlaceholder handler;


    public void addPlaceholder(@NotNull String[] path, @NotNull ParmPlaceholder placeholder) {
        PlaceholderNode currentNode = this;
        for (String key : path) {
            currentNode = currentNode.children.computeIfAbsent(key, k -> new PlaceholderNode());
        }
        currentNode.handler = placeholder;
    }

    public @Nullable String resolve(@Nullable Player player, @NotNull String params) {
        PlaceholderNode currentNode = this;
        int searchOffset = 0;
        while (true) {
            int delimiterIndex = params.indexOf('_', searchOffset);
            String key = (delimiterIndex == -1)
                    ? params.substring(searchOffset)
                    : params.substring(searchOffset, delimiterIndex);

            PlaceholderNode childNode = currentNode.children.get(key);
            if (childNode == null) {
                break;
            }
            currentNode = childNode;
            if (delimiterIndex == -1) {
                searchOffset = -1;
                break;
            }
            searchOffset = delimiterIndex + 1;
        }
        if (currentNode.handler == null) {
            return null;
        }
        String[] args;
        if (searchOffset == -1 || searchOffset >= params.length()) {
            args = new String[0];
        } else {
            String remainingParams = params.substring(searchOffset);
            args = remainingParams.split("_");
        }
        return currentNode.handler.apply(player, args);
    }
}
