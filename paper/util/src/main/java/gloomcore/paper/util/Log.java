package gloomcore.paper.util;

import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

/**
 * 日志记录工具类
 * <p>
 * 提供统一的日志记录接口，封装了SLF4J日志记录器
 * 以单例模式实现，确保整个应用使用统一的日志配置
 */
public enum Log {
    INSTANCE;
    private final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(this.getClass());
    private final Logger logger = plugin.getSLF4JLogger();

    /**
     * 获取当前实例的日志记录器
     *
     * @return Logger对象，用于记录日志信息
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * 记录一条 INFO 级别的日志。
     *
     * @param message 消息模板，使用 {} 作为占位符
     * @param args    替换占位符的参数
     */
    public void info(String message, Object... args) {
        logger.info(message, args);
    }


    /**
     * 记录一条 WARN 级别的日志。
     *
     * @param message 消息模板
     * @param args    参数
     */
    public void warn(String message, Object... args) {
        logger.warn(message, args);
    }


    /**
     * 记录一条 ERROR 级别的日志。
     *
     * @param message 消息模板
     * @param args    参数
     */
    public void error(String message, Object... args) {
        logger.error(message, args);
    }

    /**
     * 记录一条 ERROR 级别的日志，并附带完整的异常堆栈信息。
     *
     * @param message   附加的描述性消息
     * @param throwable 异常对象
     */
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    /**
     * 记录一条 DEBUG 级别的日志。
     *
     * @param message 消息模板
     * @param args    参数
     */
    public void debug(String message, Object... args) {
        logger.debug(message, args);
    }
}
