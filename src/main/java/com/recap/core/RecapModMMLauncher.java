//#if FABRIC>=1
package com.recap.core;

import org.spongepowered.asm.mixin.Mixins;

// We need to wait for MM to call us, otherwise we might initialize our mixins before it calls optifabric which would
// result in OF classes not existing while our mixins get resolved.
public class RecapModMMLauncher implements Runnable {
    private static boolean ran;

    @Override
    public void run() {
        // If this is MM2, then we're currently getting called because of our entrypoint declaration.
        // For backwards compatibility with MM1, we also declare our early_riser via custom metadata and MM2 supports
        // that as well and will therefore call us twice.
        if (ran) {
            return;
        }
        ran = true;

        Mixins.addConfiguration("mixins.compat.mapwriter.recap.json");
        Mixins.addConfiguration("mixins.compat.shaders.recap.json");
        Mixins.addConfiguration("mixins.core.recap.json");
        Mixins.addConfiguration("mixins.extras.playeroverview.recap.json");
        Mixins.addConfiguration("mixins.recording.recap.json");
        Mixins.addConfiguration("mixins.render.blend.recap.json");
        Mixins.addConfiguration("mixins.render.recap.json");
        Mixins.addConfiguration("mixins.replay.recap.json");
    }
}
//#endif
