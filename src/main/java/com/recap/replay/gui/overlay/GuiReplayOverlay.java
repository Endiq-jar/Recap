package com.recap.replay.gui.overlay;

import com.recap.core.gui.RecapIconButton;
import com.recap.core.gui.RecapSlider;
import com.recap.core.gui.RecapTheme;
import com.recap.core.events.KeyBindingEventCallback;
import com.recap.core.events.KeyEventCallback;
import com.recap.core.versions.MCVer.Keyboard;
import com.recap.replay.ReplayHandler;
import com.recap.replay.RecapModReplay;
import com.recap.replay.ReplaySender;
import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.container.AbstractGuiOverlay;
import de.johni0702.minecraft.gui.container.GuiPanel;
import de.johni0702.minecraft.gui.element.GuiElement;
import de.johni0702.minecraft.gui.element.GuiSlider;
import de.johni0702.minecraft.gui.element.GuiTooltip;
import de.johni0702.minecraft.gui.element.advanced.IGuiTimeline;
import de.johni0702.minecraft.gui.function.KeyInput;
import de.johni0702.minecraft.gui.layout.CustomLayout;
import de.johni0702.minecraft.gui.layout.HorizontalLayout;
import de.johni0702.minecraft.gui.utils.EventRegistrations;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.resource.language.I18n;


public class GuiReplayOverlay extends AbstractGuiOverlay<GuiReplayOverlay> {

    private final RecapModReplay mod = RecapModReplay.instance;

    public final GuiPanel topPanel = new GuiPanel(this)
            .setLayout(new HorizontalLayout(HorizontalLayout.Alignment.LEFT).setSpacing(5));
    /** Draws a vector play/pause glyph instead of the legacy sprite sheet icon. */
    public final RecapIconButton playPauseButton = new RecapIconButton() {
        @Override
        public GuiElement getTooltip(RenderInfo renderInfo) {
            GuiTooltip tooltip = (GuiTooltip) super.getTooltip(renderInfo);
            if (tooltip != null) {
                String label = getIcon() == RecapIconButton.Icon.PLAY
                        ? "recap.gui.ingame.menu.unpause"
                        : "recap.gui.ingame.menu.pause";
                tooltip.setText(I18n.translate(label) + " (" + mod.keyPlayPause.getBoundKey() + ")");
            }
            return tooltip;
        }
    }.setSize(22, 22).setAccent(true).setTooltip(new GuiTooltip());
    public final GuiSlider speedSlider = new RecapSlider().setSize(100, 20).setSteps(37); // 0.0 is not included
    public final GuiMarkerTimeline timeline;

    /**
     * This is not used by the replay module itself but may be used by other modules/extras to show
     * when they're active.
     */
    public final GuiPanel statusIndicatorPanel = new GuiPanel(this).setSize(100, 16)
            .setLayout(new HorizontalLayout(HorizontalLayout.Alignment.RIGHT).setSpacing(5));

    private final EventHandler eventHandler = new EventHandler();
    private boolean hidden;

    public GuiReplayOverlay(final ReplayHandler replayHandler) {
        timeline = new GuiMarkerTimeline(replayHandler){
            @Override
            public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
                setCursorPosition(replayHandler.getReplaySender().currentTimeStamp());
                super.draw(renderer, size, renderInfo);
            }
        }.setSize(Integer.MAX_VALUE, 20);

        topPanel.addElements(null, playPauseButton, speedSlider, timeline);
        setLayout(new CustomLayout<GuiReplayOverlay>() {
            @Override
            protected void layout(GuiReplayOverlay container, int width, int height) {
                pos(topPanel, 16, 11);
                size(topPanel, width - 32 - 44, 22);

                pos(statusIndicatorPanel, width / 2, height - 22);
                width(statusIndicatorPanel, width / 2 - 16);
            }
        });

        playPauseButton.onClick(new Runnable() {
            @Override
            public void run() {
                ReplaySender replaySender = replayHandler.getReplaySender();
                // If currently paused
                if (replaySender.paused()) {
                    // then play
                    replaySender.setReplaySpeed(getSpeedSliderValue());
                } else {
                    // else pause
                    replaySender.setReplaySpeed(0);
                }
            }
        });

        speedSlider.onValueChanged(new Runnable() {
            @Override
            public void run() {
                double speed = getSpeedSliderValue();
                speedSlider.setText(I18n.translate("recap.gui.speed") + ": " + speed + "x");
                ReplaySender replaySender = replayHandler.getReplaySender();
                if (!replaySender.paused()) {
                    replaySender.setReplaySpeed(speed);
                }
            }
        }).setValue(9);

        timeline.onClick(new IGuiTimeline.OnClick() {
            @Override
            public void run(int time) {
                replayHandler.doJump(time, true);
            }
        }).setLength(replayHandler.getReplayDuration());
    }

    public double getSpeedSliderValue() {
        int value = speedSlider.getValue() + 1;
        if (value <= 9) {
            return value / 10d;
        } else {
            return 1 + (0.25d * (value - 10));
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (isVisible() != visible) {
            if (visible) {
                eventHandler.register();
            } else {
                eventHandler.unregister();
            }
        }
        super.setVisible(visible);
    }

    @Override
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        // Do not render overlay if all hud, or this one specifically, is hidden and we're not in some popup
        if ((getMinecraft().options.hudHidden || hidden) && isAllowUserInput()) {
            // Note that this only applies to when the mouse is visible, otherwise
            // the draw method isn't called in the first place
            return;
        }
        if (renderInfo.getLayer() == 0) {
            // The glyph mirrors the playback state, so it is updated right before drawing.
            playPauseButton.setIcon(replayHandler.getReplaySender().paused()
                    ? RecapIconButton.Icon.PLAY : RecapIconButton.Icon.PAUSE);
            drawControlBar(renderer, size);
        }
        super.draw(renderer, size, renderInfo);
    }

    /**
     * Draws the Recap control bar behind the play/pause button, the speed slider and the timeline, so
     * that the controls stay readable on bright scenes without hiding the world.
     */
    private void drawControlBar(GuiRenderer renderer, ReadableDimension size) {
        int barX = 8;
        int barY = 6;
        int barWidth = size.getWidth() - 16;
        int barHeight = 32;

        RecapTheme.hudBar(renderer, barX, barY, barWidth, barHeight);
        // Gradient rail on the left edge marks where the control cluster starts.
        renderer.drawRect(barX, barY + 5, 2, barHeight - 10,
                RecapTheme.ACCENT, RecapTheme.ACCENT, RecapTheme.ACCENT_ALT, RecapTheme.ACCENT_ALT);

        // Small "RECAP" wordmark on the right end of the bar keeps the overlay branded without noise.
        String mark = "RECAP";
        renderer.drawString(barX + barWidth - RecapTheme.textWidth(mark) - 8,
                barY + (barHeight - RecapTheme.fontHeight()) / 2, RecapTheme.withAlpha(RecapTheme.ACCENT, 0.75f),
                mark, false);

        if (!statusIndicatorPanel.getChildren().isEmpty()) {
            int statusWidth = Math.min(160, size.getWidth() / 2);
            int x = size.getWidth() / 2 - statusWidth / 2;
            int y = size.getHeight() - 24;
            RecapTheme.hudBar(renderer, x, y, statusWidth, 18);
            renderer.drawRect(x, y + 5, 2, 8, RecapTheme.ACCENT_COOL);
        }
    }

    @Override
    protected GuiReplayOverlay getThis() {
        return this;
    }

    private class EventHandler extends EventRegistrations {
        { on(KeyBindingEventCallback.EVENT, this::onKeyBindingEvent); }
        private void onKeyBindingEvent() {
            GameOptions gameSettings = getMinecraft().options;
            while (gameSettings.keyChat.wasPressed() || gameSettings.keyCommand.wasPressed()) {
                if (!isMouseVisible()) {
                    setMouseVisible(true);
                }
            }
        }

        { on(KeyEventCallback.EVENT, this::onKeyInput); }
        private boolean onKeyInput(KeyInput keyInput, int action) {
            if (action != KeyEventCallback.ACTION_PRESS) return false;
            // Allow F1 to be used to hide the replay gui (e.g. for recording with OBS)
            if (isMouseVisible() && keyInput.key == Keyboard.KEY_F1) {
                hidden = !hidden;
                return true;
            }
            return false;
        }
    }
}
