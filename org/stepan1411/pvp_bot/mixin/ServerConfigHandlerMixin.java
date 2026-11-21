package org.stepan1411.pvp_bot.mixin;

import net.minecraft.class_3324;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(class_3324.class)
public class ServerConfigHandlerMixin {
    
    @Inject(method = "getMaxPlayerCount", at = @At("RETURN"), cancellable = true)
    private void increaseMaxPlayers(CallbackInfoReturnable<Integer> cir) {

        cir.setReturnValue(99999);
    }
}
