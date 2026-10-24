package com.recap.core.gui;

import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.container.GuiPanel;
import de.johni0702.minecraft.gui.element.GuiElement;
import de.johni0702.minecraft.gui.element.GuiLabel;
import de.johni0702.minecraft.gui.layout.CustomLayout;
import de.johni0702.minecraft.gui.layout.VerticalLayout;
import de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import de.johni0702.minecraft.gui.utils.lwjgl.Point;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;

/**
 * A themed card: a header strip with the section title, an optional accent tag on the right and a stack of
 * rows. Each row is a label on the left and its control on the right, highlighted while hovered.
 */
public class RecapCard extends GuiPanel {
    public static final int HEADER_HEIGHT = 26;
    public static final int ROW_HEIGHT = 26;
    public static final int ROW_SPACING = 2;
    public static final int CONTENT_PADDING = 8;
    public static final int CONTROL_WIDTH = 150;
    public static final int CONTROL_HEIGHT = 20;

    private final String title;
    private String tag;
    private String note;
    private final GuiPanel rows = new GuiPanel().setLayout(new VerticalLayout().setSpacing(ROW_SPACING));
    private int cardWidth = 440;

    public RecapCard(String title) {
        this.title = title;
        addElements(null, rows);
        setLayout(new CustomLayout<GuiPanel>() {
            @Override
            protected void layout(GuiPanel container, int width, int height) {
                pos(rows, CONTENT_PADDING, HEADER_HEIGHT + 6);
                size(rows, width - CONTENT_PADDING * 2, height - HEADER_HEIGHT - 10);
            }
        });
    }

    /** Small uppercase tag in the card header, e.g. a count or a category hint. */
    public RecapCard setTag(String tag) {
        this.tag = tag;
        return this;
    }

    /** A dim sentence rendered above the rows. */
    public RecapCard setNote(String note) {
        this.note = note;
        return this;
    }

    /**
     * Adds a settings row and returns the given control so it can be wired up by the caller.
     */
    public <E extends GuiElement<?>> E addRow(String label, E control) {
        GuiLabel guiLabel = new GuiLabel().setText(label).setColor(RecapTheme.TEXT_COLOR);
        GuiPanel row = new RowPanel();
        row.addElements(null, guiLabel, control);
        row.setLayout(new CustomLayout<GuiPanel>() {
            @Override
            protected void layout(GuiPanel container, int width, int height) {
                pos(guiLabel, 8, (height - RecapTheme.fontHeight()) / 2);
                size(control, CONTROL_WIDTH, CONTROL_HEIGHT);
                pos(control, width - CONTROL_WIDTH - 8, (height - CONTROL_HEIGHT) / 2);
            }
        });
        rows.addElements(new VerticalLayout.Data(0), row);
        return control;
    }

    public int getRowCount() {
        return rows.getChildren().size();
    }

    public int getPreferredHeight() {
        int noteHeight = note == null ? 0 : RecapTheme.fontHeight() + 8;
        return HEADER_HEIGHT + 10 + noteHeight + getRowCount() * (ROW_HEIGHT + ROW_SPACING);
    }

    public void setCardWidth(int cardWidth) {
        this.cardWidth = cardWidth;
    }

    @Override
    public ReadableDimension calcMinSize() {
        return new Dimension(cardWidth, getPreferredHeight());
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

            // Header: quieter surface plus the signature top gradient rail.
            renderer.drawRect(1, 1, width - 2, HEADER_HEIGHT - 1, RecapTheme.CARD_HEADER);
            renderer.drawRect(2, 0, width - 4, 1, RecapTheme.ACCENT, RecapTheme.ACCENT_ALT, RecapTheme.ACCENT, RecapTheme.ACCENT_ALT);
            renderer.drawRect(1, HEADER_HEIGHT, width - 2, 1, RecapTheme.BORDER);

            String upper = title == null ? "" : title.toUpperCase();
            renderer.drawString(10, (HEADER_HEIGHT - RecapTheme.fontHeight()) / 2, RecapTheme.TEXT, upper, false);
            renderer.drawRect(10, HEADER_HEIGHT - 3, RecapTheme.textWidth(upper), 1,
                    RecapTheme.withAlpha(RecapTheme.ACCENT, 0.55f));

            if (tag != null) {
                int tagWidth = RecapTheme.textWidth(tag) + 12;
                RecapTheme.chip(renderer, width - 10 - tagWidth, (HEADER_HEIGHT - 14) / 2, tagWidth, 14, tag, RecapTheme.ACCENT_COOL);
            }

            if (note != null) {
                renderer.drawString(10, HEADER_HEIGHT + 6, RecapTheme.TEXT_FAINT, note, false);
            }
        }
        super.draw(renderer, size, renderInfo);
    }

    /** A single row, highlighting itself while the mouse hovers over it. */
    private static class RowPanel extends GuiPanel {
        @Override
        public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
            if (renderInfo.getLayer() == 0) {
                Point mouse = new Point(renderInfo.getMouseX(), renderInfo.getMouseY());
                if (getContainer() != null) {
                    getContainer().convertFor(this, mouse);
                }
                boolean hovered = mouse.getX() >= 0 && mouse.getY() >= 0
                        && mouse.getX() < size.getWidth() && mouse.getY() < size.getHeight();
                RecapTheme.roundedRect(renderer, 0, 0, size.getWidth(), size.getHeight(),
                        hovered ? RecapTheme.ROW_HOVER : RecapTheme.ROW);
                renderer.drawRect(0, 2, 2, size.getHeight() - 4,
                        hovered ? RecapTheme.ACCENT : RecapTheme.withAlpha(RecapTheme.ACCENT, 0.25f));
            }
            super.draw(renderer, size, renderInfo);
        }
    }
}
