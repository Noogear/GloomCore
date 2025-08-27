package cn.gloomcore.paper.placeholder.fixedPlaceholder;


import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface FixedPlaceholder {

    @Nullable
    String process(@Nullable Player player);

    @Nullable
    default String process() {
        return process(null);
    }


}
