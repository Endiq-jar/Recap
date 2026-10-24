package com.recap.render.hooks;

import com.recap.render.utils.FlawlessFrames;
import net.minecraft.client.render.WorldRenderer;

public class ForceChunkLoadingHook {

    public static boolean enabled;

    private final WorldRenderer hooked;

    public ForceChunkLoadingHook(WorldRenderer renderGlobal) {
        this.hooked = renderGlobal;

        enabled = true;
        FlawlessFrames.setEnabled(true);
        //#if MC < 26.2
        IForceChunkLoading.from(renderGlobal).recapModRender_setHook(this);
        //#endif
    }

    public void uninstall() {
        //#if MC < 26.2
        IForceChunkLoading.from(hooked).recapModRender_setHook(null);
        //#endif
        FlawlessFrames.setEnabled(false);
        enabled = false;
    }

    public interface IBlockOnChunkRebuilds {
        boolean uploadEverythingBlocking();
    }
}
