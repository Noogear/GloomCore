package cn.gloomcore.paper.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.DataComponentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.jspecify.annotations.NullMarked;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@NullMarked
public class ItemBuilder {

    private final ItemStack itemStack;
    private final ItemMeta meta;

    protected ItemBuilder(ItemStack itemStack) {
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

    public <T extends ItemMeta> ItemBuilder meta(Class<T> metaClass, Consumer<T> consumer) {
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
        this.meta.displayName(name.decoration(TextDecoration.ITALIC, false));
        return this;
    }

    public ItemBuilder name(String miniMessage) {
        return name(MiniMessage.miniMessage().deserialize(miniMessage));
    }

    public ItemBuilder lore(List<? extends ComponentLike> lines) {
        List<Component> processedLines = lines.stream()
                .map(ComponentLike::asComponent)
                .map(line -> line.decoration(TextDecoration.ITALIC, false))
                .collect(Collectors.toList());
        this.meta.lore(processedLines);
        return this;
    }

    public ItemBuilder lore(ComponentLike... lines) {
        return lore(Arrays.asList(lines));
    }

    public ItemBuilder lore(String... miniMessages) {
        List<Component> components = Arrays.stream(miniMessages)
                .map(line -> MiniMessage.miniMessage().deserialize(line))
                .collect(Collectors.toList());
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

    public ItemBuilder appendLore(ComponentLike... lines) {
        return appendLore(Arrays.asList(lines));
    }

    public ItemBuilder itemModel(NamespacedKey itemModel) {
        this.meta.setItemModel(itemModel);
        return this;
    }

    public ItemBuilder customModelData(Integer data) {
        this.meta.setCustomModelData(data);
        return this;
    }

    public ItemBuilder customModelData(CustomModelDataComponent customModelDataComponent) {
        meta.setCustomModelDataComponent(customModelDataComponent);
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        this.meta.setUnbreakable(unbreakable);
        return this;
    }

    public ItemBuilder addFlags(ItemFlag... flags) {
        this.meta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder removeFlags(ItemFlag... flags) {
        this.meta.removeItemFlags(flags);
        return this;
    }

    public ItemBuilder tooltipStyle(NamespacedKey tooltipStyle) {
        this.meta.setTooltipStyle(tooltipStyle);
        return this;
    }

    public ItemBuilder hideTooltip(boolean hideTooltip) {
        this.meta.setHideTooltip(hideTooltip);
        return this;
    }

    public ItemBuilder skull(Consumer<SkullMeta> consumer) {
        return meta(SkullMeta.class, consumer);
    }

    public SkullContext skull() {
        if (this.meta instanceof SkullMeta) {
            return new SkullContext();
        }
        throw new IllegalStateException("Cannot apply skull modifications to a non-skull item (" + this.itemStack.getType() + ")");
    }

    public class SkullContext {
        private final SkullMeta skullMeta;

        private SkullContext() {
            this.skullMeta = (SkullMeta) ItemBuilder.this.meta;
        }

        public SkullContext owner(OfflinePlayer owner) {
            this.skullMeta.setOwningPlayer(owner);
            return this;
        }

        public SkullContext texture(String textureValue) {
            try {
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                profile.getProperties().add(new ProfileProperty("textures", textureValue));
                this.skullMeta.setPlayerProfile(profile);
            } catch (Exception e) {
                System.err.println("Failed to set skull texture. This feature may require Paper server.");
            }
            return this;
        }

        public SkullContext textureUrl(String url) {
            String textureJson = "{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}";
            String base64 = Base64.getEncoder().encodeToString(textureJson.getBytes());
            return texture(base64);
        }

        public ItemBuilder apply() {
            return ItemBuilder.this;
        }
    }
}
