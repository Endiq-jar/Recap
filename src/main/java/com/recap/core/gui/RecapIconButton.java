package com.recap.core.gui;

import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.container.GuiContainer;
import de.johni0702.minecraft.gui.element.GuiButton;
import de.johni0702.minecraft.gui.element.GuiElement;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;

/**
 * A square button that draws a vector glyph instead of a sprite.
 *
 * <p>The in-game overlays used to pull their icons out of the legacy texture sheet, which made
 * them look different from the rest of the interface and tied them to a fixed 20x20 grid. Drawing the
 * glyphs from rectangles keeps them crisp, themed and identical on every Minecraft version.</p>
 */
public class RecapIconButton extends GuiButton {

    public enum Icon {
        PLAY, PAUSE, KEYFRAME, TIME_KEYFRAME, ZOOM_IN, ZOOM_OUT, CLOSE, ELLIPSIS
    }

    private Icon icon = Icon.PLAY;
    private boolean accent;

    public RecapIconButton() {
    }

    public RecapIconButton(GuiContainer container) {
        super(container);
    }

    public RecapIconButton setIcon(Icon icon) {
        this.icon = icon;
        return this;
    }

    public Icon getIcon() {
        return icon;
    }

    /** Draws the button with the brand gradient instead of the quiet surface. */
    public RecapIconButton setAccent(boolean accent) {
        this.accent = accent;
        return this;
    }

    @Override
    public RecapIconButton setSize(int width, int height) {
        super.setSize(width, height);
        return this;
    }

    @Override
    public RecapIconButton onClick(Runnable onClick) {
        super.onClick(onClick);
        return this;
    }

    @Override
    public RecapIconButton setTooltip(GuiElement tooltip) {
        super.setTooltip(tooltip);
        return this;
    }

    @Override
    public RecapIconButton setDisabled() {
        super.setDisabled();
        return this;
    }

    @Override
    public RecapIconButton setI18nLabel(String label, Object... args) {
        super.setI18nLabel(label, args);
        return this;
    }

    @Override
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        int width = size.getWidth();
        int height = size.getHeight();
        boolean hovered = RecapTheme.isHovered(this, size, renderInfo);

        int background = !isEnabled() ? 0xFF161B27
                : accent ? (hovered ? RecapTheme.ACCENT_HOVER : RecapTheme.mix(RecapTheme.ACCENT, RecapTheme.ACCENT_ALT, 0.45f))
                : hovered ? RecapTheme.ROW_ACTIVE : RecapTheme.ROW;
        RecapTheme.roundedRect(renderer, 0, 0, width, height, background);
        if (!accent) {
            renderer.drawRect(1, 0, width - 2, 1, hovered ? RecapTheme.withAlpha(RecapTheme.ACCENT, 0.8f) : RecapTheme.BORDER);
            renderer.drawRect(1, height - 1, width - 2, 1, RecapTheme.BORDER);
        }

        int glyph = accent ? RecapTheme.TEXT_ON_ACCENT : (hovered ? RecapTheme.TEXT : RecapTheme.TEXT_DIM);
        int centerX = width / 2;
        int centerY = height / 2;
        int r = Math.max(4, Math.min(width, height) / 2 - 4);

        switch (icon) {
            case PLAY: {
                // Right pointing triangle made of stacked rows.
                for (int i = 0; i < r; i++) {
                    int rowWidth = Math.max(1, r - i);
                    renderer.drawRect(centerX - r / 2 + i, centerY - r + i, rowWidth, 1, glyph);
                    renderer.drawRect(centerX - r / 2 + i, centerY + r - i - 1, rowWidth, 1, glyph);
                }
                break;
            }
            case PAUSE: {
                renderer.drawRect(centerX - r + 1, centerY - r, 2, r * 2, glyph);
                renderer.drawRect(centerX + r - 3, centerY - r, 2, r * 2, glyph);
                break;
            }
            case KEYFRAME: {
                // Diamond: two mirrored staircases.
                for (int i = 0; i < r; i++) {
                    renderer.drawRect(centerX - i, centerY - r + i, i * 2 + 1, 1, glyph);
                    renderer.drawRect(centerX - i, centerY + r - i - 1, i * 2 + 1, 1, glyph);
                }
                break;
            }
            case TIME_KEYFRAME: {
                renderer.drawRect(centerX - r, centerY - r, r * 2, 2, glyph);
                renderer.drawRect(centerX - r, centerY + r - 2, r * 2, 2, glyph);
                renderer.drawRect(centerX - 1, centerY - r, 2, r * 2, glyph);
                break;
            }
            case ZOOM_IN: {
                renderer.drawRect(centerX - r, centerY - 1, r * 2, 2, glyph);
                renderer.drawRect(centerX - 1, centerY - r, 2, r * 2, glyph);
                break;
            }
            case ZOOM_OUT: {
                renderer.drawRect(centerX - r, centerY - 1, r * 2, 2, glyph);
                break;
            }
            case CLOSE: {
                for (int i = 0; i < r * 2; i++) {
                    renderer.drawRect(centerX - r + i, centerY - r + i, 1, 1, glyph);
                    renderer.drawRect(centerX + r - 1 - i, centerY - r + i, 1, 1, glyph);
                }
                break;
            }
            case ELLIPSIS:
            default: {
                renderer.drawRect(centerX - 5, centerY - 1, 2, 2, glyph);
                renderer.drawRect(centerX - 1, centerY - 1, 2, 2, glyph);
                renderer.drawRect(centerX + 3, centerY - 1, 2, 2, glyph);
                break;
            }
        }
    }
}
