package gloomcore.paper.placeholder.util.internal;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface ParmPlaceholder extends Placeholder{


    static ParmPlaceholder of(@NotNull Function<Player, String> playerOnlyFunction) {
        return (player, args) -> playerOnlyFunction.apply(player);
    }


    static ParmPlaceholder of(@NotNull BiFunction<Player, String[], String> parmFunction) {
        return parmFunction::apply;
    }

}
