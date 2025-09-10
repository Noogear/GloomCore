package gloomcore.paper.command.util;

import java.util.Objects;

/**
 * MiniMessage 渲染样式配置：可自定义树前缀、字面量、参数、描述、符号、重定向等颜色。
 * 颜色值支持 MiniMessage 颜色名或十六进制，如 "white"、"gray"、"#55FFFF"。
 */
public final class CommandTreeStyle {

    private static volatile CommandTreeStyle INSTANCE = new CommandTreeStyle();

    private String treeColor = "gray";
    private String literalColor = "white";
    private String argumentColor = "aqua";
    private String descriptionColor = "gray";
    private String symbolColor = "yellow";
    private String redirectColor = "gray";

    /**
     * 返回当前全局样式实例。
     *
     * @return 样式实例
     */
    public static CommandTreeStyle get() {
        return INSTANCE;
    }

    /**
     * 设置全局样式实例。
     *
     * @param style 新样式
     */
    public static void set(CommandTreeStyle style) {
        INSTANCE = Objects.requireNonNull(style, "style");
    }

    private static String normalize(String c) {
        if (c == null || c.isBlank()) {
            return "white";
        }
        return c.startsWith("#") ? c.toUpperCase() : c.toLowerCase();
    }

    private static String tagOpen(String name) {
        return "<" + name + ">";
    }

    private static String tagClose(String name) {
        return "</" + name + ">";
    }

    /**
     * 树枝着色起始标签。
     *
     * @return 起始标签
     */
    public String openTree() {
        return tagOpen(treeColor);
    }

    /**
     * 树枝着色结束标签。
     *
     * @return 结束标签
     */
    public String closeTree() {
        return tagClose(treeColor);
    }

    /**
     * 字面量着色起始标签。
     *
     * @return 起始标签
     */
    public String openLiteral() {
        return tagOpen(literalColor);
    }

    /**
     * 字面量着色结束标签。
     *
     * @return 结束标签
     */
    public String closeLiteral() {
        return tagClose(literalColor);
    }

    /**
     * 参数着色起始标签。
     *
     * @return 起始标签
     */
    public String openArgument() {
        return tagOpen(argumentColor);
    }

    /**
     * 参数着色结束标签。
     *
     * @return 结束标签
     */
    public String closeArgument() {
        return tagClose(argumentColor);
    }

    /**
     * 描述着色起始标签。
     *
     * @return 起始标签
     */
    public String openDescription() {
        return tagOpen(descriptionColor);
    }

    /**
     * 描述着色结束标签。
     *
     * @return 结束标签
     */
    public String closeDescription() {
        return tagClose(descriptionColor);
    }

    /**
     * 符号着色起始标签（*、~ 等）。
     *
     * @return 起始标签
     */
    public String openSymbol() {
        return tagOpen(symbolColor);
    }

    /**
     * 符号着色结束标签。
     *
     * @return 结束标签
     */
    public String closeSymbol() {
        return tagClose(symbolColor);
    }

    /**
     * 重定向标记起始标签。
     *
     * @return 起始标签
     */
    public String openRedirect() {
        return tagOpen(redirectColor);
    }

    /**
     * 重定向标记结束标签。
     *
     * @return 结束标签
     */
    public String closeRedirect() {
        return tagClose(redirectColor);
    }

    /**
     * 获取树颜色。
     *
     * @return 颜色名称/代码
     */
    public String getTreeColor() {
        return treeColor;
    }

    /**
     * 设置树颜色。
     *
     * @param treeColor 颜色
     * @return this
     */
    public CommandTreeStyle setTreeColor(String treeColor) {
        this.treeColor = normalize(treeColor);
        return this;
    }

    /**
     * 获取字面量颜色。
     *
     * @return 颜色
     */
    public String getLiteralColor() {
        return literalColor;
    }

    /**
     * 设置字面量颜色。
     *
     * @param literalColor 颜色
     * @return this
     */
    public CommandTreeStyle setLiteralColor(String literalColor) {
        this.literalColor = normalize(literalColor);
        return this;
    }

    /**
     * 获取参数颜色。
     *
     * @return 颜色
     */
    public String getArgumentColor() {
        return argumentColor;
    }

    /**
     * 设置参数颜色。
     *
     * @param argumentColor 颜色
     * @return this
     */
    public CommandTreeStyle setArgumentColor(String argumentColor) {
        this.argumentColor = normalize(argumentColor);
        return this;
    }

    /**
     * 获取描述颜色。
     *
     * @return 颜色
     */
    public String getDescriptionColor() {
        return descriptionColor;
    }

    /**
     * 设置描述颜色。
     *
     * @param descriptionColor 颜色
     * @return this
     */
    public CommandTreeStyle setDescriptionColor(String descriptionColor) {
        this.descriptionColor = normalize(descriptionColor);
        return this;
    }

    /**
     * 获取符号颜色。
     *
     * @return 颜色
     */
    public String getSymbolColor() {
        return symbolColor;
    }

    /**
     * 设置符号颜色。
     *
     * @param symbolColor 颜色
     * @return this
     */
    public CommandTreeStyle setSymbolColor(String symbolColor) {
        this.symbolColor = normalize(symbolColor);
        return this;
    }

    /**
     * 获取重定向颜色。
     *
     * @return 颜色
     */
    public String getRedirectColor() {
        return redirectColor;
    }

    /**
     * 设置重定向颜色。
     *
     * @param redirectColor 颜色
     * @return this
     */
    public CommandTreeStyle setRedirectColor(String redirectColor) {
        this.redirectColor = normalize(redirectColor);
        return this;
    }
}
