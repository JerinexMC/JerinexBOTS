package org.stepan1411.pvp_bot.fixes;

import net.minecraft.class_3222;
import org.stepan1411.pvp_bot.bot.BotUtils;

public class EatingMovementFix {

    public static void apply(class_3222 bot) {
        var state = BotUtils.getState(bot.method_5477().getString());
        if (!state.isEating) return;

        bot.method_5728(false);

        bot.field_6250 *= 0.2f;
        bot.field_6212 *= 0.2f;

        var vel = bot.method_18798();
        bot.method_18800(vel.field_1352 * 0.8, vel.field_1351, vel.field_1350 * 0.8);
    }
}
