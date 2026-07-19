package com.voidanomaly.selectivepeacefulmobs.event;

import com.voidanomaly.selectivepeacefulmobs.behavior.MobTargetingRules;
import com.voidanomaly.selectivepeacefulmobs.config.SpmConfig;
import com.voidanomaly.selectivepeacefulmobs.state.PlayerPeacefulState;
import com.voidanomaly.selectivepeacefulmobs.state.ProvocationTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class MobBehaviorEvents {
    @SubscribeEvent
    public void onMobTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Mob mob
                && event.getNewAboutToBeSetTarget() instanceof ServerPlayer player
                && MobTargetingRules.shouldIgnorePlayer(mob, player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onMobTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel)) {
            return;
        }

        LivingEntity target = mob.getTarget();
        if (target == null && mob.getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED)) {
            target = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        }

        if (target instanceof ServerPlayer player && MobTargetingRules.shouldIgnorePlayer(mob, player)) {
            MobTargetingRules.clearProtectedTarget(mob);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Mob attackedMob) || !(attackedMob.level() instanceof ServerLevel level)) {
            return;
        }

        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!PlayerPeacefulState.get(player).isPeaceful(player) || !MobTargetingRules.isHostileMob(attackedMob)) {
            return;
        }
        if (MobTargetingRules.isAlwaysHostile(attackedMob)) {
            return;
        }
        if (SpmConfig.IGNORE_CREATIVE_AND_SPECTATOR.get() && (player.isCreative() || player.isSpectator())) {
            return;
        }

        long until = level.getGameTime() + SpmConfig.PROVOKED_TIME_SECONDS.get() * 20L;
        ProvocationTracker.provoke(attackedMob, player, until);
        MobTargetingRules.setProvokedTarget(attackedMob, player);

        if (SpmConfig.GROUP_AGGRO.get()) {
            provokeNearbySameType(level, attackedMob, player, until);
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 200 == 0) {
            ProvocationTracker.cleanup(event.getServer().overworld().getGameTime());
        }
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        ProvocationTracker.clear();
    }

    private void provokeNearbySameType(ServerLevel level, Mob attackedMob, ServerPlayer player, long until) {
        double radius = SpmConfig.GROUP_AGGRO_RADIUS.get();
        double radiusSquared = radius * radius;
        AABB area = attackedMob.getBoundingBox().inflate(radius);

        for (Mob nearbyMob : level.getEntitiesOfClass(
                Mob.class,
                area,
                mob -> mob != attackedMob
                        && mob.getType() == attackedMob.getType()
                        && mob.distanceToSqr(attackedMob) <= radiusSquared
        )) {
            if (!MobTargetingRules.isHostileMob(nearbyMob) || MobTargetingRules.isAlwaysHostile(nearbyMob)) {
                continue;
            }

            ProvocationTracker.provoke(nearbyMob, player, until);
            MobTargetingRules.setProvokedTarget(nearbyMob, player);
        }
    }
}
