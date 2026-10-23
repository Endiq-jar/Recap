package com.replaymod.core.gui;

import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.utils.lwjgl.Color;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;

/**
 * Single source of truth for the Recap Mod look and feel.
 *
 * <p>The design language is deliberately flat and quiet: near black surfaces, a single amber accent and
 * hairline borders, so that the replay itself (which is what the user is looking at most of the time)
 * is never in competition with the interface.</p>
 *
 * <p>Everything here is drawn through jGui's {@link GuiRenderer}, which abstracts away the Minecraft
 * rendering API differences, so this code compiles unchanged for every supported Minecraft version.</p>
 */
public final class RecapTheme {
    private RecapTheme() {
    }

    /* ------------------------------------------------------------------ surfaces */

    public static final int BACKGROUND_TOP = 0xFF080A0E;
    public static final int BACKGROUND_BOTTOM = 0xFF141A24;

    public static final int CARD = 0xFF151B26;
    public static final int CARD_HEADER = 0xFF1B2331;
    public static final int BORDER = 0xFF27303F;

    public static final int ROW = 0xFF1A2230;
    public static final int ROW_HOVER = 0xFF222C3D;

    public static final int HUD = 0xC40B0E14;
    public static final int HUD_BORDER = 0x33FFFFFF;

    /* --------------------------------------------------------------------- brand */

    public static final int ACCENT = 0xFFF5A623;
    public static final int ACCENT_HOVER = 0xFFFFC46B;
    public static final int ACCENT_DARK = 0xFF1A1409;

    /* ---------------------------------------------------------------------- text */

    public static final int TEXT = 0xFFEDF1F7;
    public static final int TEXT_DIM = 0xFF8B94A6;
    public static final int TEXT_ON_ACCENT = 0xFF12151B;

    /* ------------------------------------------------------------------- status */

    public static final int RECORDING = 0xFFFF4D5E;
    public static final int PAUSED = 0xFFF5A623;
    public static final int SUCCESS = 0xFF5BD98A;

    /* -------------------------------------------------------------- timelines */

    /** Colour of a spectator ("camera in another entity") keyframe. */
    public static final int SPECTATOR = 0xFF7C8CFF;

    /** Colour of the interpolation segment between two position keyframes. */
    public static final int SEGMENT_POSITION = 0xFF3F76B5;

    /** Colour of the interpolation segment between two timestamped keyframes. */
    public static final int SEGMENT_TIME = 0xFF9E5568;

    /* ------------------------------------------------------------ ReadableColor view */

    public static final ReadableColor CARD_COLOR = new Color(21, 27, 38);
    public static final ReadableColor CARD_HEADER_COLOR = new Color(27, 35, 49);
    public static final ReadableColor BORDER_COLOR = new Color(39, 48, 63);
    public static final ReadableColor ROW_COLOR = new Color(26, 34, 48);
    public static final ReadableColor TEXT_COLOR = new Color(237, 241, 247);
    public static final ReadableColor TEXT_DIM_COLOR = new Color(139, 148, 166);
    public static final ReadableColor ACCENT_COLOR = new Color(245, 166, 35);
    public static final ReadableColor ACCENT_HOVER_COLOR = new Color(255, 196, 107);
    public static final ReadableColor RECORDING_COLOR = new Color(255, 77, 94);
    public static final ReadableColor SUCCESS_COLOR = new Color(91, 217, 138);
    public static final ReadableColor HUD_COLOR = new Color(11, 14, 20);

    /* ------------------------------------------------------------------- drawing */

    /**
     * Paints the full screen background: a vertical gradient with a soft vignette at the bottom so the
     * content in the middle of the screen always stays readable.
     */
    public static void screen(GuiRenderer renderer, int width, int height) {
        renderer.drawRect(0, 0, width, height,
                BACKGROUND_TOP, BACKGROUND_TOP, BACKGROUND_BOTTOM, BACKGROUND_BOTTOM);
        renderer.drawRect(0, 0, width, 48, 0x33000000, 0x33000000, 0x00000000, 0x00000000);
        renderer.drawRect(0, height - 64, width, 64, 0x00000000, 0x00000000, 0x59000000, 0x59000000);
    }

    /**
     * Draws the screen header: accent rail, title, subtitle and a right aligned badge, followed by a
     * hairline separator.
     */
    public static void header(GuiRenderer renderer, int width, String title, String subtitle, String badge) {
        if (title != null) {
            renderer.drawString(18, 16, ACCENT, title);
        }
        if (subtitle != null) {
            renderer.drawString(18, 29, TEXT_DIM, subtitle);
        }
        renderer.drawRect(14, 15, 2, 20, ACCENT);
        if (badge != null) {
            int badgeWidth = badge.length() * 5 + 8;
            roundedRect(renderer, width - 18 - badgeWidth, 16, badgeWidth, 13, 0x33F5A623);
            renderer.drawString(width - 18 - badgeWidth + 4, 19, ACCENT, badge);
        }
        renderer.drawRect(18, 44, width - 36, 1, BORDER);
        renderer.drawRect(18, 44, 48, 1, ACCENT);
    }

    /** Draws a footer hint together with its hairline separator. */
    public static void footer(GuiRenderer renderer, int width, int height, String hint) {
        renderer.drawRect(18, height - 44, width - 36, 1, BORDER);
        if (hint != null) {
            renderer.drawString(18, height - 34, TEXT_DIM, hint);
        }
    }

    /** Draws a card: filled surface, hairline border and an accent tick in the top left corner. */
    public static void card(GuiRenderer renderer, int x, int y, int width, int height) {
        renderer.drawRect(x, y, width, height, CARD);
        renderer.drawRect(x, y, width, 1, BORDER);
        renderer.drawRect(x, y + height - 1, width, 1, BORDER);
        renderer.drawRect(x, y + 1, 1, height - 2, BORDER);
        renderer.drawRect(x + width - 1, y + 1, 1, height - 2, BORDER);
        renderer.drawRect(x, y, 3, 12, ACCENT);
        renderer.drawRect(x + 1, y + 1, width - 2, 0, 0);
    }

    /** Draws the header strip of a card, including the category label. */
    public static void cardHeader(GuiRenderer renderer, int x, int y, int width, int height, String label) {
        renderer.drawRect(x, y, width, height, CARD_HEADER);
        renderer.drawRect(x, y + height - 1, width, 1, BORDER);
        if (label != null) {
            renderer.drawString(x + 10, y + (height - 8) / 2, TEXT, label.toUpperCase());
        }
    }

    /** Draws a subtle highlight behind a settings row. */
    public static void rowHighlight(GuiRenderer renderer, int x, int y, int width, int height) {
        renderer.drawRect(x, y, width, height, ROW);
    }

    /** Draws a translucent rounded HUD bar. */
    public static void hudBar(GuiRenderer renderer, int x, int y, int width, int height) {
        roundedRect(renderer, x, y, width, height, HUD);
        renderer.drawRect(x + 2, y + 1, width - 4, 1, HUD_BORDER);
    }

    /**
     * Draws a rectangle with softly chamfered (2px) corners. Alpha blending is preserved because the
     * individual rows never overlap.
     */
    public static void roundedRect(GuiRenderer renderer, int x, int y, int width, int height, int color) {
        if (width < 6 || height < 6) {
            renderer.drawRect(x, y, width, height, color);
            return;
        }
        renderer.drawRect(x + 2, y, width - 4, 1, color);
        renderer.drawRect(x + 1, y + 1, width - 2, 1, color);
        renderer.drawRect(x, y + 2, width, height - 4, color);
        renderer.drawRect(x + 1, y + height - 2, width - 2, 1, color);
        renderer.drawRect(x + 2, y + height - 1, width - 4, 1, color);
    }

    /**
     * Draws a timeline marker (keyframe or replay marker) as a filled square with a contrasting one
     * pixel border. Selected markers get a bright border so the current selection is visible even
     * when the marker sits on top of a segment line.
     */
    public static void timelineMarker(GuiRenderer renderer, int x, int y, int size, int fill, boolean selected) {
        int border = selected ? TEXT : shade(fill, -0.55f);
        if (size >= 4) {
            renderer.drawRect(x, y, size, size, border);
            renderer.drawRect(x + 1, y + 1, size - 2, size - 2, selected ? ACCENT_HOVER : fill);
        } else {
            renderer.drawRect(x, y, size, size, selected ? ACCENT_HOVER : fill);
        }
    }

    /** Returns the given ARGB color with its alpha channel scaled by {@code alpha} (0..1). */
    public static int withAlpha(int color, float alpha) {
        int a = (int) (((color >>> 24) & 0xFF) * Math.max(0f, Math.min(1f, alpha)));
        return (a << 24) | (color & 0x00FFFFFF);
    }

    /** Blends {@code color} towards black (negative amount) or white (positive amount). */
    public static int shade(int color, float amount) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        if (amount >= 0) {
            r = (int) (r + (255 - r) * amount);
            g = (int) (g + (255 - g) * amount);
            b = (int) (b + (255 - b) * amount);
        } else {
            float f = 1 + amount;
            r = (int) (r * f);
            g = (int) (g * f);
            b = (int) (b * f);
        }
        return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
    }
}
