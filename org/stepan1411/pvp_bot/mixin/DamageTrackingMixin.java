package org.stepan1411.pvp_bot.mixin;

import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_3218;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1657.class)
public class DamageTrackingMixin {

    private static final String BOT_PLAYER_CLASS = "hero.bane.herobot.bot.BotPlayer";

    @Inject(method = "applyDamage", at = @At("HEAD"))
    private void onDamageTrack(class_3218 world, class_1282 source, float amount, CallbackInfo ci) {
        if (!Float.isFinite(amount) || amount > 1000 || amount < 0) return;

        // Damage RECEIVED by bot
        if (this.getClass().getName().equals(BOT_PLAYER_CLASS)) {
            org.stepan1411.pvp_bot.stats.StatsReporter.addDamageReceived(amount);
        }

        // Damage DEALT by bot (bot is the attacker)
        class_1297 attacker = source.method_5529();
        if (attacker != null && attacker.getClass().getName().equals(BOT_PLAYER_CLASS)) {
            org.stepan1411.pvp_bot.stats.StatsReporter.addDamageDealt(amount);
        }
    }
}
