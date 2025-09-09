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

    public static CommandTreeStyle get() {
        return INSTANCE;
    }

    public static void set(CommandTreeStyle style) {
        INSTANCE = Objects.requireNonNull(style, "style");
    }

    private static String normalize(String c) {
        if (c == null || c.isBlank()) return "white";
        return c.startsWith("#") ? c.toUpperCase() : c.toLowerCase();
    }

    private static String tagOpen(String name) {
        return "<" + name + ">";
    }

    private static String tagClose(String name) {
        return "</" + name + ">";
    }

    public String openTree() {
        return tagOpen(treeColor);
    }

    public String closeTree() {
        return tagClose(treeColor);
    }

    public String openLiteral() {
        return tagOpen(literalColor);
    }

    public String closeLiteral() {
        return tagClose(literalColor);
    }

    public String openArgument() {
        return tagOpen(argumentColor);
    }

    public String closeArgument() {
        return tagClose(argumentColor);
    }

    public String openDescription() {
        return tagOpen(descriptionColor);
    }

    public String closeDescription() {
        return tagClose(descriptionColor);
    }

    public String openSymbol() {
        return tagOpen(symbolColor);
    }

    public String closeSymbol() {
        return tagClose(symbolColor);
    }

    public String openRedirect() {
        return tagOpen(redirectColor);
    }

    public String closeRedirect() {
        return tagClose(redirectColor);
    }

    public String getTreeColor() {
        return treeColor;
    }

    public CommandTreeStyle setTreeColor(String treeColor) {
        this.treeColor = normalize(treeColor);
        return this;
    }

    public String getLiteralColor() {
        return literalColor;
    }

    public CommandTreeStyle setLiteralColor(String literalColor) {
        this.literalColor = normalize(literalColor);
        return this;
    }

    public String getArgumentColor() {
        return argumentColor;
    }

    public CommandTreeStyle setArgumentColor(String argumentColor) {
        this.argumentColor = normalize(argumentColor);
        return this;
    }

    public String getDescriptionColor() {
        return descriptionColor;
    }

    public CommandTreeStyle setDescriptionColor(String descriptionColor) {
        this.descriptionColor = normalize(descriptionColor);
        return this;
    }

    public String getSymbolColor() {
        return symbolColor;
    }

    public CommandTreeStyle setSymbolColor(String symbolColor) {
        this.symbolColor = normalize(symbolColor);
        return this;
    }

    public String getRedirectColor() {
        return redirectColor;
    }

    public CommandTreeStyle setRedirectColor(String redirectColor) {
        this.redirectColor = normalize(redirectColor);
        return this;
    }
}

