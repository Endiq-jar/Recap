package com.recap.core.gui;

import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.container.GuiContainer;
import de.johni0702.minecraft.gui.element.GuiSlider;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;

/**
 * A flat slider: a recessed track, a brand gradient fill up to the current value and a capsule handle,
 * with the label centered on top.
 */
public class RecapSlider extends GuiSlider {

    /** Label shown on top of the track; jGui keeps its own copy private, so we track it here as well. */
    private String caption = "";

    public RecapSlider() {
    }

    public RecapSlider(GuiContainer container) {
        super(container);
    }

    @Override
    public RecapSlider setText(String text) {
        this.caption = text == null ? "" : text;
        super.setText(text);
        return this;
    }

    @Override
    public RecapSlider setI18nText(String text, Object... args) {
        super.setI18nText(text, args);
        this.caption = net.minecraft.client.resource.language.I18n.translate(text, args);
        return this;
    }

    @Override
    public RecapSlider setSize(int width, int height) {
        super.setSize(width, height);
        return this;
    }

    @Override
    public RecapSlider setSteps(int steps) {
        super.setSteps(steps);
        return this;
    }

    @Override
    public RecapSlider setValue(int value) {
        super.setValue(value);
        return this;
    }

    @Override
    public RecapSlider onValueChanged(Runnable runnable) {
        super.onValueChanged(runnable);
        return this;
    }

    @Override
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        int width = size.getWidth();
        int height = size.getHeight();

        int trackHeight = Math.max(3, height / 3);
        int trackY = (height - trackHeight) / 2;

        // Recessed track.
        RecapTheme.capsule(renderer, 0, trackY, width, trackHeight, 0xFF0B0E18, 0);
        renderer.drawRect(1, trackY, width - 2, 1, RecapTheme.BORDER);

        float progress = getSteps() <= 0 ? 0f : Math.max(0f, Math.min(1f, getValue() / (float) getSteps()));
        int filled = Math.round(width * progress);
        if (filled > 2) {
            renderer.drawRect(0, trackY, filled, trackHeight,
                    RecapTheme.ACCENT, RecapTheme.ACCENT, RecapTheme.ACCENT_ALT, RecapTheme.ACCENT_ALT);
        }

        // Handle.
        int handleWidth = 6;
        int handleHeight = Math.min(height, 16);
        int handleX = Math.max(0, Math.min(width - handleWidth, filled - handleWidth / 2));
        RecapTheme.capsule(renderer, handleX, (height - handleHeight) / 2, handleWidth, handleHeight,
                isEnabled() ? RecapTheme.TEXT : RecapTheme.TEXT_FAINT, 0);

        if (!caption.isEmpty()) {
            renderer.drawCenteredString(width / 2, (height - RecapTheme.fontHeight()) / 2, RecapTheme.TEXT, caption, false);
        }
    }
}
