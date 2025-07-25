package cn.gloomcore.replacer;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface StringReplacer {

    String apply(@NotNull String original);

}
