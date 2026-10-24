package com.recap.simplepathing.preview;

import com.recap.core.KeyBindingRegistry;
import com.recap.core.SettingsRegistry;
import com.recap.core.events.SettingsChangedCallback;
import de.johni0702.minecraft.gui.utils.EventRegistrations;
import com.recap.replay.ReplayHandler;
import com.recap.replay.events.ReplayClosedCallback;
import com.recap.replay.events.ReplayOpenedCallback;
import com.recap.simplepathing.RecapModSimplePathing;
import com.recap.simplepathing.Setting;

public class PathPreview extends EventRegistrations {
    private final RecapModSimplePathing mod;

    private ReplayHandler replayHandler;
    private PathPreviewRenderer renderer;

    public PathPreview(RecapModSimplePathing mod) {
        this.mod = mod;

        on(SettingsChangedCallback.EVENT, (registry, key) -> {
            if (key == Setting.PATH_PREVIEW) {
                update();
            }
        });

        on(ReplayOpenedCallback.EVENT, replayHandler -> {
            this.replayHandler = replayHandler;
            update();
        });

        on(ReplayClosedCallback.EVENT, replayHandler -> {
            this.replayHandler = null;
            update();
        });
    }

    public void registerKeyBindings(KeyBindingRegistry registry) {
        registry.registerKeyBinding("recap.input.pathpreview", KeyBindingRegistry.KeyCodes.KEY_H, () -> {
            SettingsRegistry settings = mod.getCore().getSettingsRegistry();
            settings.set(Setting.PATH_PREVIEW, !settings.get(Setting.PATH_PREVIEW));
            settings.save();
        }, true);
    }

    private void update() {
        if (mod.getCore().getSettingsRegistry().get(Setting.PATH_PREVIEW) && replayHandler != null) {
            if (renderer == null) {
                renderer = new PathPreviewRenderer(mod, replayHandler);
                renderer.register();
            }
        } else {
            if (renderer != null) {
                renderer.unregister();
                renderer = null;
            }
        }
    }
}
