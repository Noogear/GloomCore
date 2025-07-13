package cn.gloomcore.action;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@FunctionalInterface
public interface ConsumerAction<I, T> extends Action<T> {

    void run(T t, @Nullable Consumer<I> callback);

    @Override
    default void run(T t) {
        run(t, null);
    }
}
