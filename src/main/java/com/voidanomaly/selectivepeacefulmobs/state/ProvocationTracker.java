package com.voidanomaly.selectivepeacefulmobs.state;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class ProvocationTracker {
    private static final Map<MobPlayerPair, Long> PROVOKED_UNTIL_TICK = new HashMap<>();

    public static void provoke(LivingEntity mob, ServerPlayer player, long untilGameTime) {
        PROVOKED_UNTIL_TICK.put(new MobPlayerPair(mob.getUUID(), player.getUUID()), untilGameTime);
    }

    public static boolean isProvoked(LivingEntity mob, ServerPlayer player, long gameTime) {
        MobPlayerPair pair = new MobPlayerPair(mob.getUUID(), player.getUUID());
        Long until = PROVOKED_UNTIL_TICK.get(pair);
        if (until == null) {
            return false;
        }
        if (until <= gameTime) {
            PROVOKED_UNTIL_TICK.remove(pair);
            return false;
        }
        return true;
    }

    public static void cleanup(long gameTime) {
        Iterator<Map.Entry<MobPlayerPair, Long>> iterator = PROVOKED_UNTIL_TICK.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue() <= gameTime) {
                iterator.remove();
            }
        }
    }

    public static void clear() {
        PROVOKED_UNTIL_TICK.clear();
    }

    private record MobPlayerPair(UUID mobId, UUID playerId) {}

    private ProvocationTracker() {}
}
