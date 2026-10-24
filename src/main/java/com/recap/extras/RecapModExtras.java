package com.recap.extras;

import com.recap.core.Module;
import com.recap.core.RecapMod;
import com.recap.extras.advancedscreenshots.AdvancedScreenshots;
import com.recap.extras.playeroverview.PlayerOverview;
import com.recap.extras.youtube.YoutubeUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RecapModExtras implements Module {
    { instance = this; }
    public static RecapModExtras instance;

    private static final List<Class<? extends Extra>> builtin = Arrays.asList(
            AdvancedScreenshots.class,
            PlayerOverview.class,
            YoutubeUpload.class,
            FullBrightness.class,
            QuickMode.class,
            HotkeyButtons.class
    );

    private final Map<Class<? extends Extra>, Extra> instances = new HashMap<>();

    public static Logger LOGGER = LogManager.getLogger();

    public RecapModExtras(RecapMod core) {
        core.getSettingsRegistry().register(Setting.class);
    }

    @Override
    public void initClient() {
        for (Class<? extends Extra> cls : builtin) {
            try {
                Extra extra = cls.newInstance();
                extra.register(RecapMod.instance);
                instances.put(cls, extra);
            } catch (Throwable t) {
                LOGGER.warn("Failed to load extra " + cls.getName() + ": ", t);
            }
        }
    }

    public <T extends Extra> Optional<T> get(Class<T> cls) {
        return Optional.ofNullable(instances.get(cls)).map(cls::cast);
    }
}
