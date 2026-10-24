//#if MC>=10800
package com.recap.compat.shaders.mixin;

import com.recap.compat.shaders.ShaderReflection;
import com.recap.replay.ReplayHandler;
import com.recap.replay.RecapModReplay;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinShaderEntityRenderer {

    //#if MC>=11400
    @Inject(method = "renderWorld", at = @At("HEAD"))
    //#else
    //#if MC>=11400
    //$$ @Inject(method = "updateCameraAndRender(FJ)V", at = @At("HEAD"))
    //#else
    //$$ @Inject(method = "renderWorldPass", at = @At("HEAD"))
    //#endif
    //#endif
    private void recapModCompat_updateShaderFrameTimeCounter(CallbackInfo ignore) {
        if (RecapModReplay.instance.getReplayHandler() == null) return;
        if (ShaderReflection.shaders_frameTimeCounter == null) return;

        ReplayHandler replayHandler = RecapModReplay.instance.getReplayHandler();
        float timestamp = replayHandler.getReplaySender().currentTimeStamp() / 1000f % 3600f;
        try {
            ShaderReflection.shaders_frameTimeCounter.set(null, timestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
//#endif
