package gloomcore.contract.builder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试 IBuilder 接口的基本功能
 * 这是一个示例测试类，展示了现代 Java 测试的最佳实践
 */
class IBuilderTest {

    /**
     * 测试构建器的基本功能
     */
    @Test
    @DisplayName("应该能够创建并配置构建器")
    void shouldCreateAndConfigureBuilder() {
        // 这是一个示例测试，展示了测试结构
        // 在实际项目中，您需要根据具体的实现来编写测试
        assertNotNull(MockBuilder.create());
    }

    /**
     * 测试条件配置功能
     */
    @Test
    @DisplayName("应该能够根据条件配置构建器")
    void shouldConfigureConditionally() {
        MockBuilder builder = MockBuilder.create();
        
        // 测试条件为真的情况
        builder.configureIf(true, b -> b.setValue("configured"));
        assertEquals("configured", builder.getValue());
        
        // 测试条件为假的情况
        builder.configureIf(false, b -> b.setValue("not configured"));
        assertEquals("configured", builder.getValue()); // 应该保持原值
    }

    /**
     * 示例构建器实现，用于测试
     */
    private static class MockBuilder implements IBuilder<MockBuilder, String> {
        private String value = "default";

        public static MockBuilder create() {
            return new MockBuilder();
        }

        public MockBuilder setValue(String value) {
            this.value = value;
            return this;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String build() {
            return value;
        }

        @Override
        public MockBuilder configure(java.util.function.Consumer<MockBuilder> builderConsumer) {
            builderConsumer.accept(this);
            return this;
        }

        @Override
        public MockBuilder copy() {
            MockBuilder copy = new MockBuilder();
            copy.value = this.value;
            return copy;
        }
    }
}