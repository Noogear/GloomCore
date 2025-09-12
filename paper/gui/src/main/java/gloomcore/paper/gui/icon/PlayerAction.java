package gloomcore.paper.gui.icon;

import gloomcore.contract.action.Action;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

/**
 * 玩家动作函数式接口
 * <p>
 * 该接口表示一个针对玩家执行的动作，继承自Action接口，
 * 并将泛型参数指定为Player类型。
 * </p>
 */
@NullMarked
@FunctionalInterface
public interface PlayerAction extends Action<Player, Void> {
}
