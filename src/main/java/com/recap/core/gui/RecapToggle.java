package com.recap.core.gui;

import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.container.GuiContainer;
import de.johni0702.minecraft.gui.element.GuiToggleButton;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;

/**
 * A two-to-three state switch drawn as a capsule with a sliding knob.
 *
 * <p>Unlike the vanilla toggle it does not repeat the label inside the widget: the label belongs to the
 * row that owns the control, and the switch itself only shows the currently selected value.</p>
 */
public class RecapToggle<V> extends GuiToggleButton<V> {

    private static final int KNOB_PADDING = 2;

    public RecapToggle() {
    }

    public RecapToggle(GuiContainer container) {
        super(container);
    }

    @Override
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        int width = size.getWidth();
        int height = size.getHeight();
        boolean hovered = RecapTheme.isHovered(this, size, renderInfo);
        int states = Math.max(1, getValues() == null ? 1 : getValues().length);
        boolean on = getSelected() == 0;

        int track = !isEnabled() ? 0xFF1A1F2E
                : on ? RecapTheme.mix(RecapTheme.ACCENT, RecapTheme.ACCENT_ALT, 0.5f)
                : 0xFF232A3C;
        if (hovered && !on) {
            track = 0xFF2E3648;
        }

        RecapTheme.capsule(renderer, 0, 0, width, height, track,
                isEnabled() ? RecapTheme.withAlpha(on ? RecapTheme.ACCENT_HOVER : RecapTheme.BORDER, 0.8f) : RecapTheme.BORDER);

        // Knob position: for a two state switch it slides fully across, for more states it divides the track.
        int knobSize = height - KNOB_PADDING * 2;
        float progress = states <= 1 ? 0f : getSelected() / (float) (states - 1);
        int travel = width - knobSize - KNOB_PADDING * 2;
        int knobX = KNOB_PADDING + Math.round(travel * (1f - progress));

        int knobColor = isEnabled() ? RecapTheme.TEXT : RecapTheme.TEXT_FAINT;
        RecapTheme.capsule(renderer, knobX, KNOB_PADDING, knobSize, knobSize, knobColor, 0);

        String label = getValues() == null ? null : String.valueOf(getSelectedValue());
        if (label != null && width > 60) {
            int color = on && isEnabled() ? 0xFF0B0E18 : RecapTheme.TEXT_DIM;
            int textX = on ? 8 : knobX + knobSize + 2;
            if (textX + RecapTheme.textWidth(label) < width - 2) {
                renderer.drawString(textX, (height - RecapTheme.fontHeight()) / 2, color, label, false);
            }
        }
    }
}
