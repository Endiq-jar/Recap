package com.recap.render.hooks;

import net.minecraft.client.renderer.RenderGlobal;

public class ForceChunkLoadingHook {
    private final RenderGlobal hooked;

    public ForceChunkLoadingHook(RenderGlobal renderGlobal) {
        this.hooked = renderGlobal;

        IForceChunkLoading.from(renderGlobal).recapModRender_setHook(this);
    }

    public void uninstall() {
        IForceChunkLoading.from(hooked).recapModRender_setHook(null);
    }
}
