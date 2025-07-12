package cn.gloomcore.action;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Supplier;

public class ActionGroup implements PlayerAction {
    private final List<PlayerAction> actions;

    public ActionGroup(Supplier<List<PlayerAction>> supplier) {
        this(supplier.get());
    }

    public ActionGroup(List<PlayerAction> actions) {
        this.actions = actions;
    }

    public ActionGroup() {
        this(new ArrayList<>());
    }

    public ActionGroup addAction(PlayerAction... action) {
        actions.addAll(List.of(action));
        return this;
    }

    @Override
    public void run(Player player, BooleanConsumer callback) {
        RunContext context = new RunContext(player, new LinkedList<>(actions));
        context.start();
    }

    private record RunContext(Player player, Queue<PlayerAction> queue) {

        void start() {
            runNext();
        }

        private void runNext() {
            if (queue.isEmpty()) {
                return;
            }
            PlayerAction nextAction = queue.poll();
            nextAction.run(player, success -> {
                if (success) {
                    runNext();
                } else {
                    queue.clear();
                }

            });
        }
    }
}

