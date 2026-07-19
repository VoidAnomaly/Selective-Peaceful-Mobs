package com.voidanomaly.selectivepeacefulmobs.mixin;

import com.voidanomaly.selectivepeacefulmobs.behavior.MobTargetingRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(
            method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void selectivePeacefulMobs$skipProtectedPlayers(
            LivingEntity target,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if ((Object) this instanceof Mob mob
                && target instanceof ServerPlayer player
                && MobTargetingRules.shouldIgnorePlayer(mob, player)) {
            callback.setReturnValue(false);
        }
    }
}
