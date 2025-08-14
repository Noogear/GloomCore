package cn.gloomcore.placeholder.placeholders.impl;

import cn.gloomcore.placeholder.placeholders.CacheText;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.UUID;

public class IntervalPlayerPlaceholder extends PlayerPlaceholder{
    private final Object2ObjectOpenHashMap<UUID, CacheText> cacheTexts;
    private final long intervalMillis;

    public IntervalPlayerPlaceholder(String original) {
        super(original);
    }
}
