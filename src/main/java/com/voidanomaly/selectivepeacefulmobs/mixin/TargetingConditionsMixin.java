package com.voidanomaly.selectivepeacefulmobs.mixin;

import com.voidanomaly.selectivepeacefulmobs.behavior.MobTargetingRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TargetingConditions.class)
public abstract class TargetingConditionsMixin {
    @Shadow
    @Final
    private boolean isCombat;

    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    private void selectivePeacefulMobs$skipProtectedPlayers(
            LivingEntity attacker,
            LivingEntity target,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (isCombat
                && attacker instanceof Mob mob
                && target instanceof ServerPlayer player
                && MobTargetingRules.shouldIgnorePlayer(mob, player)) {
            callback.setReturnValue(false);
        }
    }
}
