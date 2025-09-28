package gloomcore.adventure.i18n;

import java.util.*;

public class LanguageMapResolver {

    private final Map<String, String> originalMap;
    private final Map<String, List<Token>> parsedTemplates = new HashMap<>();
    private final Map<String, String> resolvedCache = new HashMap<>();
    private final Set<String> recursionStack = new HashSet<>();

    /**
     * 构造函数，在此进行预编译。
     */
    public LanguageMapResolver(Map<String, String> map) {
        this.originalMap = map;
        for (Map.Entry<String, String> entry : originalMap.entrySet()) {
            parsedTemplates.put(entry.getKey(), parse(entry.getValue()));
        }
    }

    /**
     * 将一个字符串模板解析为Token列表。
     */
    private List<Token> parse(String template) {
        List<Token> tokens = new ArrayList<>();
        if (template == null || template.isEmpty()) {
            return tokens; // 处理空或null模板的边界情况
        }

        int lastIndex = 0;
        while (lastIndex < template.length()) {
            int start = template.indexOf('<', lastIndex);
            if (start == -1) {
                tokens.add(new LiteralToken(template.substring(lastIndex)));
                break;
            }

            int end = template.indexOf('>', start + 1);
            if (end == -1) {
                tokens.add(new LiteralToken(template.substring(lastIndex)));
                break;
            }

            if (start > lastIndex) {
                tokens.add(new LiteralToken(template.substring(lastIndex, start)));
            }

            String placeholderKey = template.substring(start + 1, end);
            tokens.add(new PlaceholderToken(placeholderKey));

            lastIndex = end + 1;
        }
        return tokens;
    }

    /**
     * 执行解析并返回包含所有解析后值的新Map。
     */
    public Map<String, String> resolveAll() {
        Map<String, String> resultMap = new HashMap<>();
        for (String key : originalMap.keySet()) {
            resultMap.put(key, resolve(key));
        }
        return resultMap;
    }

    /**
     * 解析单个键的最终值。
     */
    private String resolve(String key) {
        if (resolvedCache.containsKey(key)) {
            return resolvedCache.get(key);
        }

        if (!parsedTemplates.containsKey(key)) {
            return "<" + key + ">";
        }

        if (recursionStack.contains(key)) {
            throw new IllegalStateException("Circular dependency detected involving key: " + key + ". Path: " + recursionStack);
        }
        recursionStack.add(key);

        List<Token> tokensToProcess = parsedTemplates.get(key);
        if (tokensToProcess.size() == 1) {
            Token singleToken = tokensToProcess.getFirst();
            if (singleToken instanceof LiteralToken(String text)) {
                recursionStack.remove(key);
                resolvedCache.put(key, text);
                return text;
            }
        }

        StringBuilder newValueBuilder = new StringBuilder(64);

        for (Token token : tokensToProcess) {
            if (token instanceof LiteralToken(String text)) {
                newValueBuilder.append(text);
            } else if (token instanceof PlaceholderToken(String key1)) {
                newValueBuilder.append(resolve(key1));
            }
        }

        recursionStack.remove(key);

        String resolvedValue = newValueBuilder.toString();
        resolvedCache.put(key, resolvedValue);

        return resolvedValue;
    }

    private interface Token {
    }

    private record LiteralToken(String text) implements Token {
    }

    private record PlaceholderToken(String key) implements Token {
    }
}
