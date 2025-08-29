package cn.gloomcore.action;

import cn.gloomcore.replacer.StringReplacer;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

@FunctionalInterface
public interface Action {

    @NotNull
    static Action combine(@NotNull Collection<? extends Action> actions) {
        return new Action() {

            @Override
            public void run(@Nullable Player player, @Nullable BooleanConsumer callback, @Nullable StringReplacer replacer) {
                RunContext context = new RunContext(new LinkedList<>(actions), player, callback, replacer);
                context.start();
            }

            private record RunContext(Queue<Action> queue, Player player, BooleanConsumer callback,
                                      StringReplacer replacer) {
                void start() {
                    runNext();
                }

                private void runNext() {
                    if (queue.isEmpty()) {
                        callback.accept(true);
                        return;
                    }
                    Action nextAction = queue.poll();
                    nextAction.run(player, success -> {
                        if (success) {
                            runNext();
                        } else {
                            queue.clear();
                            callback.accept(false);
                        }
                    }, replacer);
                }
            }
        };
    }

    @NotNull
    static Action combine(@NotNull Action... actions) {
        return combine(Arrays.asList(actions));
    }

    @NotNull
    static Action of(Consumer<Player> action) {
        return (p, c, r) -> action.accept(p);
    }

//    void run();
//
//    default  void run(@Nullable Player player){
//        run();
//    }
//
//    default  void run(@Nullable Player player, @Nullable BooleanConsumer callback){
//        run(player);
//    }
//
//    default  void run(@Nullable Player player, @Nullable BooleanConsumer callback,@Nullable StringReplacer replacer){
//        run(player, callback);
//    }


    void run(@Nullable Player player, @Nullable BooleanConsumer callback, @Nullable StringReplacer replacer);

    default void run(@Nullable Player player, @Nullable BooleanConsumer callback) {
        run(player, callback, null);
    }

    default void run(@Nullable Player player, @Nullable StringReplacer replacer) {
        run(player, null, replacer);
    }

    default void run(@Nullable Player player) {
        run(player, null, null);
    }


}
