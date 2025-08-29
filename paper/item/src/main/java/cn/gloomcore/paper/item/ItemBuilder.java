package cn.gloomcore.paper.item;

import io.papermc.paper.datacomponent.DataComponentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NullMarked;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@NullMarked
public class ItemBuilder {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    private final ItemStack itemStack;
    private final ItemMeta meta;

    private ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        this.meta = this.itemStack.getItemMeta();
    }

    private ItemBuilder(ItemBuilder other) {
        this.itemStack = other.itemStack.clone();
        this.meta = other.meta.clone();
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(new ItemStack(material));
    }

    public static ItemBuilder of(Material material, int amount) {
        return new ItemBuilder(new ItemStack(material, amount));
    }

    public static ItemBuilder of(ItemStack itemStack) {
        return new ItemBuilder(itemStack);
    }

    public static ItemBuilder copyOf(ItemBuilder builder) {
        return new ItemBuilder(builder);
    }

    public ItemStack build() {
        this.itemStack.setItemMeta(this.meta);
        return this.itemStack.clone();
    }

    public void buildAndThen(Consumer<ItemStack> consumer) {
        consumer.accept(build());
    }

    public <T extends ItemMeta> ItemBuilder asMeta(Class<T> metaClass, Consumer<T> consumer) {
        if (metaClass.isInstance(this.meta)) {
            consumer.accept(metaClass.cast(this.meta));
        }
        return this;
    }

    public ItemBuilder meta(Consumer<ItemMeta> consumer) {
        consumer.accept(this.meta);
        return this;
    }

    public <T> @Nullable T data(DataComponentType.Valued<T> type) {
        return itemStack.getData(type);
    }

    public <T> Optional<T> optional(DataComponentType.Valued<T> type) {
        return Optional.ofNullable(itemStack.getData(type));
    }

    public <T> ItemBuilder data(DataComponentType.Valued<T> type, T value) {
        itemStack.setData(type, value);
        return this;
    }

    public ItemBuilder data(DataComponentType.NonValued type) {
        itemStack.setData(type);
        return this;
    }

    public ItemBuilder resetData(DataComponentType type) {
        itemStack.resetData(type);
        return this;
    }

    public ItemBuilder unsetData(DataComponentType type) {
        itemStack.unsetData(type);
        return this;
    }

    public ItemBuilder amount(int amount) {
        this.itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder name(Component name) {
        this.meta.displayName(name.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        return this;
    }

    public ItemBuilder name(String miniMessageName) {
        return name(miniMessage.deserialize(miniMessageName));
    }

    public ItemBuilder lore(List<? extends ComponentLike> lines) {
        List<Component> processedLines = lines.stream()
                .map(ComponentLike::asComponent)
                .map(line -> line.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .toList();
        this.meta.lore(processedLines);
        return this;
    }

    public ItemBuilder lore(ComponentLike... lines) {
        return lore(Arrays.asList(lines));
    }

    public ItemBuilder lore(String... miniMessages) {
        List<Component> components = Arrays.stream(miniMessages)
                .map(miniMessage::deserialize)
                .toList();
        return lore(components);
    }

    public ItemBuilder appendLore(List<? extends ComponentLike> lines) {
        List<Component> currentLore = this.meta.lore();
        if (currentLore == null) {
            currentLore = new ArrayList<>();
        }
        lines.stream()
                .map(ComponentLike::asComponent)
                .map(line -> line.decoration(TextDecoration.ITALIC, false))
                .forEach(currentLore::add);
        this.meta.lore(currentLore);
        return this;
    }

}
