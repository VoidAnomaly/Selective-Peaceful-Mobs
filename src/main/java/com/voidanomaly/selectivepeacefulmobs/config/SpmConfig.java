package com.voidanomaly.selectivepeacefulmobs.config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class SpmConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue DEFAULT_PEACEFUL;
    public static final ModConfigSpec.BooleanValue REMEMBER_PLAYER_CHOICE;
    public static final ModConfigSpec.IntValue PROVOKED_TIME_SECONDS;
    public static final ModConfigSpec.BooleanValue GROUP_AGGRO;
    public static final ModConfigSpec.IntValue GROUP_AGGRO_RADIUS;
    public static final ModConfigSpec.BooleanValue IGNORE_CREATIVE_AND_SPECTATOR;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ALWAYS_HOSTILE_MOBS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("general");

        DEFAULT_PEACEFUL = builder
                .comment("If true, players who have not chosen yet start with peaceful mobs enabled.")
                .define("defaultPeaceful", true);

        REMEMBER_PLAYER_CHOICE = builder
                .comment("If true, player choices are saved in the world. If false, the defaultPeaceful value is always used.")
                .define("rememberPlayerChoice", true);

        PROVOKED_TIME_SECONDS = builder
                .comment("How long mobs remain angry at a peaceful player after that player attacks them.")
                .defineInRange("provokedTimeSeconds", 30, 1, 3600);

        GROUP_AGGRO = builder
                .comment("If true, nearby hostile mobs of the same type also become provoked when a player attacks one mob.")
                .define("groupAggro", true);

        GROUP_AGGRO_RADIUS = builder
                .comment("Radius in blocks used for group aggro.")
                .defineInRange("groupAggroRadius", 16, 1, 128);

        IGNORE_CREATIVE_AND_SPECTATOR = builder
                .comment("If true, creative and spectator players are always ignored by hostile mobs.")
                .define("ignoreCreativeAndSpectator", true);

        builder.pop();

        builder.push("alwayshostilemobs");

        ALWAYS_HOSTILE_MOBS = builder
                .comment(
                        "Entity ids for mobs that can always target peaceful players.",
                        "Add vanilla or modded entity ids here, for example \"minecraft:wither\".")
                .defineListAllowEmpty(
                        "mobs",
                        List.of("minecraft:ender_dragon", "minecraft:warden", "minecraft:wither"),
                        () -> "minecraft:wither",
                        SpmConfig::isEntityId);

        builder.pop();

        SPEC = builder.build();
    }

    private static boolean isEntityId(Object value) {
        if (!(value instanceof String entityId)) {
            return false;
        }

        return ResourceLocation.tryParse(entityId) != null;
    }

    private SpmConfig() {}
}
