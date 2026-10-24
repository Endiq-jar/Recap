package com.recap.extras;

import com.recap.core.KeyBindingRegistry;
import com.recap.core.RecapMod;
import com.recap.replay.ReplayHandler;
import com.recap.replay.RecapModReplay;
import com.recap.replay.events.ReplayOpenedCallback;
import com.recap.replay.gui.overlay.GuiReplayOverlay;
import de.johni0702.minecraft.gui.element.GuiImage;
import de.johni0702.minecraft.gui.layout.HorizontalLayout;
import de.johni0702.minecraft.gui.utils.EventRegistrations;

public class QuickMode extends EventRegistrations implements Extra {
    private RecapModReplay module;

    private final GuiImage indicator = new GuiImage().setTexture(RecapMod.TEXTURE, 40, 100, 16, 16).setSize(16, 16);

    @Override
    public void register(final RecapMod mod) {
        this.module = RecapModReplay.instance;

        mod.getKeyBindingRegistry().registerKeyBinding("recap.input.quickmode", KeyBindingRegistry.KeyCodes.KEY_Q, () -> {
            ReplayHandler replayHandler = module.getReplayHandler();
            if (replayHandler == null) {
                return;
            }
            replayHandler.getReplaySender().setSyncModeAndWait();
            mod.runLaterWithoutLock(() -> {
                replayHandler.ensureQuickModeInitialized(() -> {
                    boolean enabled = !replayHandler.isQuickMode();
                    updateIndicator(replayHandler.getOverlay(), enabled);
                    replayHandler.setQuickMode(enabled);
                    replayHandler.getReplaySender().setAsyncMode(true);
                });
            });
        }, true);

        register();
    }

    {
        on(ReplayOpenedCallback.EVENT, replayHandler -> updateIndicator(replayHandler.getOverlay(), replayHandler.isQuickMode()));
    }

    private void updateIndicator(GuiReplayOverlay overlay, boolean enabled) {
        if (enabled) {
            overlay.statusIndicatorPanel.addElements(new HorizontalLayout.Data(1), indicator);
        } else {
            overlay.statusIndicatorPanel.removeElement(indicator);
        }
    }
}
