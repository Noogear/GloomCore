package cn.gloomcore.item;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface BaseModify<T> {

    @NotNull T modify(@NotNull T original);

}
