package com.recap.render.hooks;

import net.minecraft.client.render.WorldRenderer;

public interface IForceChunkLoading {
    void recapModRender_setHook(ForceChunkLoadingHook hook);

    static IForceChunkLoading from(WorldRenderer worldRenderer) {
        return (IForceChunkLoading) worldRenderer;
    }
}
