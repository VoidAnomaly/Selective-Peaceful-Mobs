package com.voidanomaly.selectivepeacefulmobs.behavior;

import com.voidanomaly.selectivepeacefulmobs.config.SpmConfig;
import com.voidanomaly.selectivepeacefulmobs.state.PlayerPeacefulState;
import com.voidanomaly.selectivepeacefulmobs.state.ProvocationTracker;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

public final class MobTargetingRules {
    public static boolean shouldIgnorePlayer(Mob mob, ServerPlayer player) {
        if (!isHostileMob(mob)) {
            return false;
        }

        if (SpmConfig.IGNORE_CREATIVE_AND_SPECTATOR.get() && (player.isCreative() || player.isSpectator())) {
            return true;
        }

        if (isAlwaysHostile(mob) || !PlayerPeacefulState.get(player).isPeaceful(player)) {
            return false;
        }

        return !ProvocationTracker.isProvoked(mob, player, mob.level().getGameTime());
    }

    public static boolean isHostileMob(LivingEntity entity) {
        return entity instanceof Enemy || entity.getType().getCategory() == MobCategory.MONSTER;
    }

    public static boolean isAlwaysHostile(Mob mob) {
        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        return SpmConfig.ALWAYS_HOSTILE_MOBS.get().stream()
                .map(ResourceLocation::tryParse)
                .anyMatch(mobId::equals);
    }

    public static void setProvokedTarget(Mob mob, ServerPlayer player) {
        if (mob.getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED)) {
            LivingChangeTargetEvent event = CommonHooks.onLivingChangeTarget(
                    mob,
                    player,
                    LivingChangeTargetEvent.LivingTargetType.BEHAVIOR_TARGET
            );
            if (!event.isCanceled() && event.getNewAboutToBeSetTarget() != null) {
                mob.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, event.getNewAboutToBeSetTarget());
                mob.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            }
        } else {
            mob.setTarget(player);
        }
    }

    public static void clearProtectedTarget(Mob mob) {
        mob.goalSelector.getAvailableGoals().forEach(goal -> goal.stop());
        mob.targetSelector.getAvailableGoals().forEach(goal -> goal.stop());
        if (mob.level() instanceof ServerLevel level) {
            stopBrain(mob, level);
        }
        mob.setTarget(null);
        mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        mob.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        mob.getNavigation().stop();
        mob.setAggressive(false);
        if (mob instanceof Creeper creeper) {
            creeper.setSwellDir(-1);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void stopBrain(Mob mob, ServerLevel level) {
        Brain brain = mob.getBrain();
        brain.stopAll(level, mob);
    }

    private MobTargetingRules() {}
}
