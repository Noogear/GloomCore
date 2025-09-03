package gloomcore.paper.placeholder.internal;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface ParmPlaceholder extends Placeholder {


    static ParmPlaceholder of(@NotNull Function<String[], String> parmFunction) {
        return (player, args) -> parmFunction.apply(args);
    }


    static ParmPlaceholder of(@NotNull BiFunction<Player, String[], String> parmFunction) {
        return parmFunction::apply;
    }

}
