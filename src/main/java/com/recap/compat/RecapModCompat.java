package com.recap.compat;

import com.recap.compat.optifine.DisableFastRender;
import com.recap.core.Module;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//#if MC<11400
//$$ import com.recap.compat.oranges17animations.HideInvisibleEntities;
//#endif

//#if MC<11400
//$$ import com.recap.compat.bettersprinting.DisableBetterSprinting;
//#endif

//#if MC>=10800
import com.recap.compat.shaders.ShaderBeginRender;
//#endif

public class RecapModCompat implements Module {
    public static Logger LOGGER = LogManager.getLogger();

    @Override
    public void initClient() {
        //#if MC>=10800
        new ShaderBeginRender().register();
        //#endif
        new DisableFastRender().register();
        //#if MC<11400
        //$$ new HideInvisibleEntities().register();
        //#endif
        //#if MC<11400
        //$$ DisableBetterSprinting.register();
        //#endif
    }

}
