//#if MC>=11400
package com.recap.recording.mixin;

import com.recap.replay.InputReplayTimer;
import com.recap.replay.RecapModReplay;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Mouse.class)
public abstract class MixinMouseHelper {
    @Shadow
    private boolean cursorLocked;

    @Inject(method = "lockCursor", at = @At("HEAD"), cancellable = true)
    private void noGrab(CallbackInfo ci) {
        // Used to be provided by Forge for 1.12.2 and below
        if (Boolean.valueOf(System.getProperty("fml.noGrab", "false"))) {
            this.cursorLocked = true;
            ci.cancel();
        }
    }

    @Inject(method = "onMouseScroll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSpectator()Z"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private void handleRecapModScroll(
            long _p0, double _p1, double _p2,
            CallbackInfo ci,
            //#if MC>=12002
            //$$ boolean discreteScroll,
            //$$ double sensitivity,
            //$$ double xOffsetAccumulated,
            //$$ double yOffsetAccumulated
            //#else
            double _l1,
            //#if MC>=11802
            //$$ int yOffsetAccumulated
            //#elseif MC>=11400
            float yOffsetAccumulated
            //#else
            //$$ double yOffsetAccumulated
            //#endif
            //#endif
    ) {
        if (RecapModReplay.instance.getReplayHandler() != null) {
            InputReplayTimer.handleScroll((int) (yOffsetAccumulated * 120));
            ci.cancel();
        }
    }
}
//#endif
