package net.blueberrymcmods.viablueberry.common.util;

import com.viaversion.viaversion.libs.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import com.viaversion.viaversion.libs.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class RemappingUtil {
    public static int swordId(String id) {
        // https://github.com/ViaVersion/ViaVersion/blob/8de26a0ad33f5b739f5394ed80f69d14197fddc7/common/src/main/java/us/myles/ViaVersion/protocols/protocol1_9to1_8/Protocol1_9To1_8.java#L86
        return switch (id) {
            case "minecraft:iron_sword" -> 267;
            case "minecraft:wooden_sword" -> 268;
            case "minecraft:golden_sword" -> 272;
            case "minecraft:diamond_sword" -> 276;
            case "minecraft:stone_sword" -> 283;
            default -> 0;
        };
    }

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('§')
            .extractUrls()
            .build();

    public static String legacyToJson(String legacy) {
        return GsonComponentSerializer.gson().serialize(LEGACY_SERIALIZER.deserialize(legacy));
    }
}
