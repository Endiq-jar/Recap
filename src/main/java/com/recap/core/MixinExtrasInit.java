//#if FABRIC>=1
package com.recap.core;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class MixinExtrasInit implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        //#if MC>=11604
        com.llamalad7.mixinextras.MixinExtrasBootstrap.init();
        //#endif
    }
}
//#endif
