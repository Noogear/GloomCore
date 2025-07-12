package cn.gloomcore.scheduler.common;

import java.util.function.BooleanSupplier;

public enum Platform {
    FOLIA("io.papermc.paper.threadedregions.RegionizedServer"),
    PAPER("com.destroystokyo.paper.PaperConfig"),
    BUKKIT(() -> true);

    private final boolean isPlatform;

    Platform(BooleanSupplier isPlatformCheck) {
        this.isPlatform = isPlatformCheck.getAsBoolean();
    }

    Platform(String className) {
        this(() -> {
            try {
                Class.forName(className);
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        });
    }

    public boolean isPlatform() {
        return isPlatform;
    }
}
