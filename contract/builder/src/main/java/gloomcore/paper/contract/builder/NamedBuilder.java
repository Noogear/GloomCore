package gloomcore.paper.contract.builder;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 一个高性能、按名称分发的建造者（工厂）。
 * <p>
 * 它通过注册时提供的名称或别名，将一个“名称”和“原始值”的组合映射到一个特定的构建函数，
 * 从而高效地生成最终值。这在需要根据配置字符串动态创建对象的场景中非常有用。
 *
 * @param <R> 原始值（输入）的类型
 * @param <V> 最终值（输出）的类型
 */
public class NamedBuilder<R, V> {

    /**
     * Key: 函数的名称或别名 (统一转换为小写，以实现不区分大小写的匹配)
     * Value: 实际的构建函数
     */
    private final Map<String, BiFunction<String, R, V>> functionMap = new HashMap<>();

    /**
     * 用于记录原始注册信息，如果需要保留注册顺序或获取原始名称列表时使用。
     */
    private final List<BuilderRegistration<R, V>> registrations = new ArrayList<>();

    /**
     * 注册一个接收“名称”和“原始值”的构建函数。
     *
     * @param biFunction 构建函数
     * @param names      关联到此函数的至少一个名称或别名
     * @return 代表此次注册信息的对象
     */
    public BuilderRegistration<R, V> register(BiFunction<String, R, V> biFunction, String... names) {
        if (names == null || names.length == 0) {
            throw new IllegalArgumentException("构建函数必须至少关联一个名称。");
        }

        // 将所有名称和别名映射到同一个函数实例
        for (String name : names) {
            String lowerCaseName = name.toLowerCase(Locale.ROOT);
            if (functionMap.containsKey(lowerCaseName)) {
                System.err.println("警告: 名称或别名 '" + lowerCaseName + "' 已被注册，旧的将被覆盖。");
            }
            functionMap.put(lowerCaseName, biFunction);
        }

        BuilderRegistration<R, V> registration = new BuilderRegistration<>(biFunction, Arrays.asList(names));
        registrations.add(registration);
        return registration;
    }

    /**
     * 注册一个只接收“原始值”的构建函数。
     *
     * @param function 构建函数
     * @param names    关联到此函数的至少一个名称或别名
     * @return 代表此次注册信息的对象
     */
    public BuilderRegistration<R, V> register(Function<R, V> function, String... names) {
        // 将 Function<R, V> 适配为 BiFunction<String, R, V>
        return register((_, rawValue) -> function.apply(rawValue), names);
    }

    /**
     * 注册一个不接收任何参数的构建函数（供应者）。
     *
     * @param supplier 构建函数
     * @param names    关联到此函数的至少一个名称或别名
     * @return 代表此次注册信息的对象
     */
    public BuilderRegistration<R, V> register(Supplier<V> supplier, String... names) {
        // 将 Supplier<V> 适配为 BiFunction<String, R, V>
        return register((_, _) -> supplier.get(), names);
    }

    /**
     * 根据名称和原始值构建最终值。
     * <p>
     * 这是一个高性能的 O(1) 查找操作。
     *
     * @param name     函数的名称或别名（不区分大小写）
     * @param rawValue 传递给函数的原始值
     * @return 如果找到匹配的函数则返回包含结果的 {@link Optional}，否则返回空 {@link Optional}
     */
    public Optional<V> build(String name, R rawValue) {
        BiFunction<String, R, V> function = functionMap.get(name.toLowerCase(Locale.ROOT));
        if (function == null) {
            return Optional.empty();
        }
        // 使用 Optional.ofNullable 包装函数返回值，以安全处理函数可能返回 null 的情况
        return Optional.ofNullable(function.apply(name, rawValue));
    }

    /**
     * 根据一个包含“名称-原始值”的 Map，批量构建最终值的 Map。
     *
     * @param rawMap 包含多个构建任务的 Map
     * @return 一个包含成功构建的“名称-最终值”的 Map
     */
    public Map<String, V> build(Map<String, R> rawMap) {
        Map<String, V> finalMap = new LinkedHashMap<>();
        rawMap.forEach((name, raw) ->
                build(name, raw).ifPresent(v -> finalMap.put(name, v))
        );
        return finalMap;
    }

    /**
     * 清空所有已注册的构建函数。
     */
    public void clear() {
        functionMap.clear();
        registrations.clear();
    }

    /**
     * 获取已注册的“名称-构建函数”映射的不可变视图。
     * <p>
     * 这是一个高效的操作，直接返回内部 Map 的视图。
     *
     * @return 不可变的映射视图
     */
    public Map<String, BiFunction<String, R, V>> getRegisteredMap() {
        return Collections.unmodifiableMap(functionMap);
    }

    /**
     * 获取所有原始注册信息的不可变列表。
     *
     * @return 不可变的注册信息列表
     */
    public List<BuilderRegistration<R, V>> getRegistrations() {
        return Collections.unmodifiableList(registrations);
    }

    /**
     * 代表一次构建函数的注册信息。
     * 使用 record 简化了数据类的定义。
     *
     * @param function 构建逻辑
     * @param names    与该构建逻辑关联的所有名称和别名
     * @param <R>      原始值的类型
     * @param <V>      最终值的类型
     */
    public record BuilderRegistration<R, V>(BiFunction<String, R, V> function, List<String> names) {
    }
}
