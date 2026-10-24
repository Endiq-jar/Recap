package com.recap.core.gui;

import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.container.GuiPanel;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;

/**
 * A panel which paints the Recap card chrome behind its children.
 *
 * <p>Used by popups and dialogs which keep their own layout but must not look like a leftover vanilla
 * window. The optional title is rendered as an uppercase section header with the accent hairline.</p>
 */
public class RecapPanel extends GuiPanel {

    public static final int HEADER_HEIGHT = 26;

    private final String title;
    private String hint;
    private boolean headerEnabled;

    public RecapPanel() {
        this(null);
    }

    public RecapPanel(String title) {
        this.title = title;
        this.headerEnabled = title != null;
    }

    /** Enables/disables the header strip (a plain surface is drawn when disabled). */
    public RecapPanel setHeaderEnabled(boolean headerEnabled) {
        this.headerEnabled = headerEnabled;
        return this;
    }

    /** A dim line rendered in the header, right aligned. */
    public RecapPanel setHint(String hint) {
        this.hint = hint;
        return this;
    }

    /** Vertical space the header takes away from the children. */
    public int getHeaderHeight() {
        return headerEnabled ? HEADER_HEIGHT : 0;
    }

    @Override
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        if (renderInfo.getLayer() == 0) {
            int width = size.getWidth();
            int height = size.getHeight();

            RecapTheme.roundedRect(renderer, 0, 0, width, height, RecapTheme.CARD);
            renderer.drawRect(1, 0, width - 2, 1, RecapTheme.BORDER);
            renderer.drawRect(1, height - 1, width - 2, 1, RecapTheme.BORDER);
            renderer.drawRect(0, 2, 1, height - 4, RecapTheme.BORDER);
            renderer.drawRect(width - 1, 2, 1, height - 4, RecapTheme.BORDER);

            if (headerEnabled) {
                renderer.drawRect(1, 1, width - 2, HEADER_HEIGHT - 1, RecapTheme.CARD_HEADER);
                renderer.drawRect(2, 0, width - 4, 1,
                        RecapTheme.ACCENT, RecapTheme.ACCENT_ALT, RecapTheme.ACCENT, RecapTheme.ACCENT_ALT);
                renderer.drawRect(1, HEADER_HEIGHT, width - 2, 1, RecapTheme.BORDER);
                renderer.drawString(10, (HEADER_HEIGHT - RecapTheme.fontHeight()) / 2,
                        RecapTheme.TEXT, title == null ? "" : title.toUpperCase(), false);
                if (hint != null) {
                    int hintWidth = RecapTheme.textWidth(hint) + 12;
                    RecapTheme.chip(renderer, width - 10 - hintWidth, (HEADER_HEIGHT - 14) / 2, hintWidth, 14,
                            hint, RecapTheme.ACCENT_COOL);
                }
            }
        }
        super.draw(renderer, size, renderInfo);
    }
}
