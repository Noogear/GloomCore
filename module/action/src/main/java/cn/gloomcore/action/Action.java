package cn.gloomcore.action;

@FunctionalInterface
public interface Action<T> {

    void run(T t);

}
