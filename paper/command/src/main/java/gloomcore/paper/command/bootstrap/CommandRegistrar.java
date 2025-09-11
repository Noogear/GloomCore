package gloomcore.paper.command.bootstrap;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import gloomcore.paper.command.interfaces.CommandNode;
import gloomcore.paper.command.util.CommandTreeCache;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Objects;

/**
 * 命令注册辅助：在 Brigadier 注册完成后写入命令树缓存。
 * 使用位置：主插件在 LifecycleEvents.COMMANDS 事件回调中，构建 roots 后调用。
 */
public final class CommandRegistrar {

    private CommandRegistrar() {
    }

    public static void install(JavaPlugin plugin, CommandRootsProvider provider) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(provider, "provider");

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            // 每次命令重载都会触发此事件，因此需要重新构建根节点
            Collection<? extends CommandNode> roots = provider.buildRoots();
            // 注册并缓存，同时输出文字版命令树
            registerRootsAndCache(roots, event.registrar().getDispatcher());
            // 若需要：也可以通过缓存服务自行获取：
            // String tree = CommandTreeCache.getInstance().getCombined();
        });
    }

    /**
     * 将所有根命令注册到 Brigadier，并在注册结束后缓存命令树文本。
     */
    public static void registerRootsAndCache(Collection<? extends CommandNode> roots,
                                             CommandDispatcher<CommandSourceStack> dispatcher) {
        Objects.requireNonNull(roots, "roots");
        Objects.requireNonNull(dispatcher, "dispatcher");

        // 注册到 Brigadier
        for (CommandNode root : roots) {
            ArgumentBuilder<CommandSourceStack, ?> builder = root.build();
            dispatcher.getRoot().addChild(builder.build());
        }

        // 缓存并可选输出
        CommandTreeCache.INSTANCE.cacheAll(roots);
    }
}

