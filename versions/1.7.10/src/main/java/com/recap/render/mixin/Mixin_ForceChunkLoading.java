package com.recap.render.mixin;

import com.recap.render.hooks.ForceChunkLoadingHook;
import com.recap.render.hooks.IForceChunkLoading;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(RenderGlobal.class)
public abstract class Mixin_ForceChunkLoading implements IForceChunkLoading {
    private ForceChunkLoadingHook recapModRender_hook;

    @Override
    public void recapModRender_setHook(ForceChunkLoadingHook hook) {
        this.recapModRender_hook = hook;
    }

    @ModifyVariable(method = "updateRenderers", at = @At("HEAD"), argsOnly = true)
    private boolean recapModRender_updateAllChunks(boolean renderAllChunks) {
        if (recapModRender_hook != null) {
            renderAllChunks = true;
        }
        return renderAllChunks;
    }
}

