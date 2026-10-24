package com.recap.extras.advancedscreenshots;

import com.recap.core.RecapMod;
import com.recap.extras.Extra;

public class AdvancedScreenshots implements Extra {

    private RecapMod mod;

    @Override
    public void register(RecapMod mod) {
        this.mod = mod;
    }

    private static AdvancedScreenshots instance; { instance = this; }
    public static void take() {
        if (instance != null) {
            instance.takeScreenshot();
        }
    }

    private void takeScreenshot() {
        RecapMod.instance.runLater(() -> new GuiCreateScreenshot(mod).open());
    }
}
