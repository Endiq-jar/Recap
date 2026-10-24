package com.recap.core.gui;

import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.container.GuiContainer;
import de.johni0702.minecraft.gui.element.GuiCheckbox;
import de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;

/**
 * A checkbox drawn as a small rounded box with a gradient tick and a label to its right.
 */
public class RecapCheckbox extends GuiCheckbox {

    private static final int BOX_SIZE = 12;

    public RecapCheckbox() {
    }

    public RecapCheckbox(GuiContainer container) {
        super(container);
    }

    @Override
    public ReadableDimension calcMinSize() {
        // The themed box is wider than the vanilla one and the label sits further to the right, so the
        // element has to reserve that space or neighbouring widgets start to overlap.
        String label = getLabel();
        int width = BOX_SIZE + 8 + (label == null ? 0 : RecapTheme.textWidth(label));
        return new Dimension(width, Math.max(RecapTheme.fontHeight() + 2, BOX_SIZE + 2));
    }

    @Override
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        int height = size.getHeight();
        int boxY = (height - BOX_SIZE) / 2;
        boolean hovered = RecapTheme.isHovered(this, size, renderInfo);

        if (isChecked()) {
            RecapTheme.roundedRect(renderer, 0, boxY, BOX_SIZE, BOX_SIZE,
                    RecapTheme.mix(RecapTheme.ACCENT, RecapTheme.ACCENT_ALT, 0.4f));
            // Tick mark.
            renderer.drawRect(3, boxY + 5, 2, 3, RecapTheme.TEXT);
            renderer.drawRect(5, boxY + 3, 2, 3, RecapTheme.TEXT);
            renderer.drawRect(7, boxY + 5, 2, 3, RecapTheme.TEXT);
            renderer.drawRect(9, boxY + 3, 2, 3, RecapTheme.TEXT);
        } else {
            RecapTheme.roundedRect(renderer, 0, boxY, BOX_SIZE, BOX_SIZE, hovered ? RecapTheme.ROW_ACTIVE : 0xFF0B0E18);
            renderer.drawRect(1, boxY, BOX_SIZE - 2, 1, RecapTheme.BORDER);
            renderer.drawRect(1, boxY + BOX_SIZE - 1, BOX_SIZE - 2, 1, RecapTheme.BORDER);
        }

        String label = getLabel();
        if (label != null) {
            int color = isEnabled() ? (hovered ? RecapTheme.TEXT : RecapTheme.TEXT_DIM) : RecapTheme.TEXT_FAINT;
            renderer.drawString(BOX_SIZE + 6, (height - RecapTheme.fontHeight()) / 2, color, label, false);
        }
    }
}
