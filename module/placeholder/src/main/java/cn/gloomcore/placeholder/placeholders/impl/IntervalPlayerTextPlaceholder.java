package cn.gloomcore.placeholder.placeholders.impl;

import cn.gloomcore.placeholder.CacheText;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.UUID;

public class IntervalPlayerTextPlaceholder extends PlayerTextPlaceholder {
    private final Object2ObjectOpenHashMap<UUID, CacheText> cacheTexts;
    private final long intervalMillis;

    public IntervalPlayerTextPlaceholder(String original) {
        super(original);
    }
}
