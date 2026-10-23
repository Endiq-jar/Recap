package com.replaymod.core.gui;

import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.container.GuiPanel;
import de.johni0702.minecraft.gui.element.GuiElement;
import de.johni0702.minecraft.gui.element.GuiLabel;
import de.johni0702.minecraft.gui.layout.CustomLayout;
import de.johni0702.minecraft.gui.layout.VerticalLayout;
import de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;

/**
 * A themed card: header strip with a title, followed by a list of settings rows.
 * Each row is a label on the left and its control on the right.
 */
public class RecapCard extends GuiPanel {
    public static final int HEADER_HEIGHT = 24;
    public static final int ROW_HEIGHT = 24;
    public static final int ROW_SPACING = 2;
    public static final int CONTENT_PADDING = 8;
    public static final int CONTROL_WIDTH = 150;
    public static final int CONTROL_HEIGHT = 20;

    private final String title;
    private final GuiPanel rows = new GuiPanel().setLayout(new VerticalLayout().setSpacing(ROW_SPACING));
    private int cardWidth = 440;

    public RecapCard(String title) {
        this.title = title;
        addElements(null, rows);
        setLayout(new CustomLayout<GuiPanel>() {
            @Override
            protected void layout(GuiPanel container, int width, int height) {
                pos(rows, CONTENT_PADDING, HEADER_HEIGHT + 4);
                size(rows, width - CONTENT_PADDING * 2, height - HEADER_HEIGHT - 8);
            }
        });
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
                pos(guiLabel, 6, (height - de.johni0702.minecraft.gui.versions.MCVer.getFontRenderer().fontHeight) / 2);
                size(control, CONTROL_WIDTH, CONTROL_HEIGHT);
                pos(control, width - CONTROL_WIDTH - 6, (height - CONTROL_HEIGHT) / 2);
            }
        });
        rows.addElements(new VerticalLayout.Data(0), row);
        return control;
    }

    public int getRowCount() {
        return rows.getChildren().size();
    }

    public int getPreferredHeight() {
        return HEADER_HEIGHT + 8 + getRowCount() * (ROW_HEIGHT + ROW_SPACING);
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
            RecapTheme.card(renderer, 0, 0, size.getWidth(), size.getHeight());
            RecapTheme.cardHeader(renderer, 0, 0, size.getWidth(), HEADER_HEIGHT, title);
        }
        super.draw(renderer, size, renderInfo);
    }

    /**
     * A single row, highlighting itself while the mouse hovers over it.
     */
    private static class RowPanel extends GuiPanel {
        @Override
        public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
            if (renderInfo.getLayer() == 0) {
                int mouseX = renderInfo.getMouseX();
                int mouseY = renderInfo.getMouseY();
                boolean hovered = mouseX >= 0 && mouseY >= 0
                        && mouseX < size.getWidth() && mouseY < size.getHeight();
                renderer.drawRect(0, 0, size.getWidth(), size.getHeight(),
                        hovered ? RecapTheme.ROW_HOVER : RecapTheme.ROW);
            }
            super.draw(renderer, size, renderInfo);
        }
    }
}
