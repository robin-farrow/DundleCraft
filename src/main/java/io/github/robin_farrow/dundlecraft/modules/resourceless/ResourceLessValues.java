package io.github.robin_farrow.dundlecraft.modules.resourceless;

public final class ResourceLessValues
{
    public static final int CROP_GROWTH_SLOWDOWN = 4;
    public static final int TREE_GROWTH_SLOWDOWN = 16;

    public static final int BREED_COOLDOWN = 72000; // 1 hour (vanilla 6000 = 5 min)
    public static final int BABY_GROWTH = 72000; // 1 hour (vanilla 24000 = 20 min)
    public static final int EGG_LAY_BASE = 36000; // eggs every 30–60 min, average 1 hour (vanilla 6000 = 5–10 min)

    // Fishing (ticks): lure wait range; the fish then takes another 20–80 ticks to swim in
    public static final int FISHING_LURE_MIN = 600;
    public static final int FISHING_LURE_MAX = 900;

    public static final float PLAYER_EXHAUSTION_MULTIPLIER = 4.0F;

    private ResourceLessValues() {}
}
