package com.recap.core.gui;

import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.container.GuiContainer;
import de.johni0702.minecraft.gui.element.GuiElement;
import de.johni0702.minecraft.gui.utils.lwjgl.Color;
import de.johni0702.minecraft.gui.utils.lwjgl.Point;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import de.johni0702.minecraft.gui.versions.MCVer;

/**
 * The Recap Mod design system.
 *
 * <p>Everything the mod draws goes through this class, so the whole interface (screens, popups, HUD
 * overlays, timelines) shares one identity. The look is "ink and neon": very dark, slightly blue surfaces,
 * a thin violet-to-indigo accent, hairline borders and capsule shaped controls. Nothing is textured - every
 * surface is a plain rectangle - which is what makes it render identically on every supported Minecraft
 * version (1.21 through 26.4) instead of depending on vanilla widget sprites that change between releases.</p>
 */
public final class RecapTheme {
    private RecapTheme() {
    }

    /* ------------------------------------------------------------------ surfaces */

    public static final int BACKGROUND_TOP = 0xFF06070C;
    public static final int BACKGROUND_BOTTOM = 0xFF0D1120;

    /** Solid card surface (used where a widget needs an opaque background). */
    public static final int CARD = 0xFF12151F;
    public static final int CARD_HEADER = 0xFF171B28;
    public static final int BORDER = 0xFF262E42;

    public static final int ROW = 0xFF1A1F2E;
    public static final int ROW_HOVER = 0xFF232A3C;
    public static final int ROW_ACTIVE = 0xFF2A3350;

    /** Translucent panel used for in-game overlays, so the world stays visible behind it. */
    public static final int HUD = 0xD20A0D16;
    public static final int HUD_BORDER = 0x44FFFFFF;

    /* --------------------------------------------------------------------- brand */

    /** Primary accent (indigo). */
    public static final int ACCENT = 0xFF5B7CFA;
    /** The second half of every accent gradient. */
    public static final int ACCENT_ALT = 0xFFA855F7;
    public static final int ACCENT_HOVER = 0xFF7E9AFF;
    public static final int ACCENT_DARK = 0xFF141A33;
    /** Mint/teal used for "this is good / running" states. */
    public static final int ACCENT_COOL = 0xFF35E0D0;

    /* ---------------------------------------------------------------------- text */

    public static final int TEXT = 0xFFE9ECF5;
    public static final int TEXT_DIM = 0xFF8A93AC;
    public static final int TEXT_FAINT = 0xFF5B647C;
    public static final int TEXT_ON_ACCENT = 0xFFFFFFFF;

    /* ------------------------------------------------------------------- status */

    public static final int RECORDING = 0xFFFF5C7A;
    public static final int PAUSED = 0xFFFFC857;
    public static final int SUCCESS = 0xFF4ADE9B;
    public static final int DANGER = 0xFFFF5C7A;

    /* -------------------------------------------------------------- timelines */

    /** Colour of a spectator ("camera in another entity") keyframe. */
    public static final int SPECTATOR = 0xFF35E0D0;

    /** Colour of the interpolation segment between two position keyframes. */
    public static final int SEGMENT_POSITION = 0xFF4E6AD6;

    /** Colour of the interpolation segment between two timestamped keyframes. */
    public static final int SEGMENT_TIME = 0xFF9B5BD6;

    /* ------------------------------------------------------------ ReadableColor view */

    public static final ReadableColor CARD_COLOR = color(CARD);
    public static final ReadableColor CARD_HEADER_COLOR = color(CARD_HEADER);
    public static final ReadableColor BORDER_COLOR = color(BORDER);
    public static final ReadableColor ROW_COLOR = color(ROW);
    public static final ReadableColor TEXT_COLOR = color(TEXT);
    public static final ReadableColor TEXT_DIM_COLOR = color(TEXT_DIM);
    public static final ReadableColor TEXT_FAINT_COLOR = color(TEXT_FAINT);
    public static final ReadableColor ACCENT_COLOR = color(ACCENT);
    public static final ReadableColor ACCENT_HOVER_COLOR = color(ACCENT_HOVER);
    public static final ReadableColor ACCENT_ALT_COLOR = color(ACCENT_ALT);
    public static final ReadableColor RECORDING_COLOR = color(RECORDING);
    public static final ReadableColor DANGER_COLOR = color(DANGER);
    public static final ReadableColor SUCCESS_COLOR = color(SUCCESS);
    public static final ReadableColor HUD_COLOR = color(CARD);
    public static final ReadableColor TEXT_ON_ACCENT_COLOR = color(TEXT_ON_ACCENT);

    private static ReadableColor color(int argb) {
        return new Color((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >>> 24) & 0xFF);
    }

    /* ------------------------------------------------------------------- drawing */

    /**
     * Paints the full screen background: an ink gradient, a violet glow behind the header, a faint grid and
     * a vignette. Everything is drawn from plain rectangles so it stays byte-identical across versions.
     */
    public static void screen(GuiRenderer renderer, int width, int height) {
        renderer.drawRect(0, 0, width, height,
                BACKGROUND_TOP, BACKGROUND_TOP, BACKGROUND_BOTTOM, BACKGROUND_BOTTOM);

        // Violet bloom in the top left corner, fading out.
        glow(renderer, 0, 0, Math.max(120, width / 2), Math.max(90, height / 3), ACCENT_ALT);

        // Faint 32px grid: gives the flat background some texture without competing with the content.
        for (int x = 0; x < width; x += 32) {
            renderer.drawRect(x, 0, 1, height, 0x0CFFFFFF);
        }
        for (int y = 0; y < height; y += 32) {
            renderer.drawRect(0, y, width, 1, 0x0CFFFFFF);
        }

        // Vignette at the bottom so footer text always stays readable.
        renderer.drawRect(0, height - 72, width, 72, 0x00000000, 0x00000000, 0x66000000, 0x66000000);
    }

    /** Draws a soft radial-ish glow out of a few stacked, fading rectangles. */
    public static void glow(GuiRenderer renderer, int x, int y, int width, int height, int color) {
        int steps = 6;
        for (int i = steps; i > 0; i--) {
            int inset = (steps - i) * Math.max(4, width / (steps * 3));
            int alpha = (int) (0.055f * i / steps * 255) + 4;
            int c = withAlpha(color, alpha / 255f);
            renderer.drawRect(x + inset, y + inset / 2, width - inset * 2, height - inset,
                    c, c, 0x00000000, 0x00000000);
        }
    }

    /**
     * The screen top bar: brand mark on the left, version chip on the right, hairline underneath.
     * Screens that draw their own chrome call this together with {@link #titleBlock}.
     */
    public static final int TOP_BAR_HEIGHT = 34;

    public static void topBar(GuiRenderer renderer, int width, String version) {
        renderer.drawRect(0, 0, width, TOP_BAR_HEIGHT, 0xB30B0E18, 0xB30B0E18, 0x660B0E18, 0x660B0E18);
        renderer.drawRect(0, TOP_BAR_HEIGHT - 1, width, 1, BORDER);
        // Accent gradient rail along the very top of the window.
        renderer.drawRect(0, 0, width, 2, ACCENT, ACCENT_ALT, ACCENT, ACCENT_ALT);

        // Brand mark: small rounded tile with a light "R" bar, then the wordmark.
        roundedRect(renderer, 10, 8, 18, 18, ACCENT);
        renderer.drawRect(14, 12, 2, 10, TEXT_ON_ACCENT);
        renderer.drawRect(16, 12, 4, 2, TEXT_ON_ACCENT);
        renderer.drawRect(16, 16, 3, 2, TEXT_ON_ACCENT);
        renderer.drawString(34, 13, TEXT, "RECAP");

        if (version != null) {
            int chipWidth = textWidth(version) + 12;
            chip(renderer, width - 12 - chipWidth, 10, chipWidth, 14, version, ACCENT_ALT);
        }
    }

    /**
     * The title block below the top bar: title, subtitle and an accent rule that visually anchors the
     * content area.
     */
    public static void titleBlock(GuiRenderer renderer, int width, String title, String subtitle) {
        if (title != null) {
            renderer.drawString(18, 44, TEXT, title);
        }
        if (subtitle != null) {
            renderer.drawString(18, 57, TEXT_DIM, subtitle);
        }
        renderer.drawRect(18, 70, 34, 2, ACCENT, ACCENT_ALT, ACCENT, ACCENT_ALT);
        renderer.drawRect(52, 70, width - 70, 1, BORDER);
    }

    /** The mod version as displayed in the top bar, or {@code null} while the mod is still starting. */
    public static String version() {
        try {
            com.recap.core.RecapMod mod = com.recap.core.RecapMod.instance;
            return mod == null ? null : "v" + mod.getVersion();
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Draws the complete screen chrome (background, top bar, title block). Screens which are not derived
     * from {@code RecapScreen} use this so that every screen still looks identical.
     */
    public static void chrome(GuiRenderer renderer, int width, int height, String title, String subtitle) {
        screen(renderer, width, height);
        topBar(renderer, width, version());
        titleBlock(renderer, width, title, subtitle);
    }

    /**
     * Legacy header used by the first generation of Recap screens: draws the title block plus a right
     * aligned badge. Kept so every screen can be migrated at its own pace.
     */
    public static void header(GuiRenderer renderer, int width, String title, String subtitle, String badge) {
        if (badge != null) {
            int badgeWidth = textWidth(badge) + 12;
            chip(renderer, width - 18 - badgeWidth, 46, badgeWidth, 14, badge, ACCENT);
        }
        titleBlock(renderer, width, title, subtitle);
    }

    /** Draws a footer hint together with its hairline separator and the ESC hint on the right. */
    public static void footer(GuiRenderer renderer, int width, int height, String hint) {
        renderer.drawRect(18, height - 34, width - 36, 1, BORDER);
        if (hint != null) {
            renderer.drawString(18, height - 26, TEXT_FAINT, hint);
        }
        String esc = "ESC";
        int escWidth = textWidth(esc) + 14;
        capsule(renderer, width - 18 - escWidth, height - 28, escWidth, 14, 0x1AFFFFFF, BORDER);
        renderer.drawCenteredString(width - 18 - escWidth / 2, height - 24, TEXT_DIM, esc);
    }

    /** Draws a card: filled surface, hairline border, accent hairline along the top edge. */
    public static void card(GuiRenderer renderer, int x, int y, int width, int height) {
        renderer.drawRect(x, y, width, height, CARD);
        renderer.drawRect(x, y, width, 1, BORDER);
        renderer.drawRect(x, y + height - 1, width, 1, BORDER);
        renderer.drawRect(x, y + 1, 1, height - 2, BORDER);
        renderer.drawRect(x + width - 1, y + 1, 1, height - 2, BORDER);
        // Signature hairline: gradient from indigo to violet across the top edge.
        renderer.drawRect(x, y, width, 1, ACCENT, ACCENT_ALT, ACCENT, ACCENT_ALT);
    }

    /** Draws the header strip of a card, including the category label. */
    public static void cardHeader(GuiRenderer renderer, int x, int y, int width, int height, String label) {
        renderer.drawRect(x + 1, y + 1, width - 2, height, CARD_HEADER);
        renderer.drawRect(x, y + height, width, 1, BORDER);
        if (label != null) {
            renderer.drawString(x + 10, y + (height - fontHeight()) / 2, TEXT, label.toUpperCase());
            renderer.drawRect(x + 10, y + height - 4, textWidth(label.toUpperCase()), 1, withAlpha(ACCENT, 0.5f));
        }
    }

    /** Draws a subtle highlight behind a settings row. */
    public static void rowHighlight(GuiRenderer renderer, int x, int y, int width, int height) {
        renderer.drawRect(x, y, width, height, ROW);
    }

    /** Draws a translucent HUD bar (used by the in-game overlays). */
    public static void hudBar(GuiRenderer renderer, int x, int y, int width, int height) {
        roundedRect(renderer, x, y, width, height, HUD);
        renderer.drawRect(x + 2, y, width - 4, 1, withAlpha(ACCENT, 0.45f));
        renderer.drawRect(x + 2, y + height - 1, width - 4, 1, 0x22FFFFFF);
    }

    /** A horizontal progress bar with the brand gradient. */
    public static void progressBar(GuiRenderer renderer, int x, int y, int width, int height, float fraction) {
        float clamped = Math.max(0f, Math.min(1f, fraction));
        roundedRect(renderer, x, y, width, height, 0xFF0B0E18);
        renderer.drawRect(x, y, width, 1, BORDER);
        renderer.drawRect(x, y + height - 1, width, 1, BORDER);
        int filled = (int) ((width - 2) * clamped);
        if (filled > 0) {
            renderer.drawRect(x + 1, y + 1, filled, height - 2, ACCENT, ACCENT_ALT, ACCENT, ACCENT_ALT);
        }
    }

    /** A small capsule shaped tag, e.g. "REC", "LIVE" or a version number. */
    public static void chip(GuiRenderer renderer, int x, int y, int width, int height, String text, int color) {
        capsule(renderer, x, y, width, height, withAlpha(color, 0.16f), withAlpha(color, 0.55f));
        renderer.drawCenteredString(x + width / 2, y + (height - fontHeight()) / 2, color, text);
    }

    /** A capsule (fully rounded ends) background with a border. */
    public static void capsule(GuiRenderer renderer, int x, int y, int width, int height, int fill, int border) {
        if (width < 4 || height < 4) {
            renderer.drawRect(x, y, width, height, fill);
            return;
        }
        renderer.drawRect(x + 2, y, width - 4, height, fill);
        renderer.drawRect(x + 1, y + 1, width - 2, height - 2, fill);
        renderer.drawRect(x, y + 2, width, height - 4, fill);
        if (border != 0) {
            renderer.drawRect(x + 2, y, width - 4, 1, border);
            renderer.drawRect(x + 2, y + height - 1, width - 4, 1, border);
            renderer.drawRect(x + 1, y + 1, 1, height - 2, border);
            renderer.drawRect(x + width - 2, y + 1, 1, height - 2, border);
            renderer.drawRect(x, y + 2, 1, height - 4, border);
            renderer.drawRect(x + width - 1, y + 2, 1, height - 4, border);
        }
    }

    /** Draws a rectangle with softly chamfered (3px) corners. */
    public static void roundedRect(GuiRenderer renderer, int x, int y, int width, int height, int color) {
        if (width < 8 || height < 8) {
            renderer.drawRect(x, y, width, height, color);
            return;
        }
        renderer.drawRect(x + 3, y, width - 6, 1, color);
        renderer.drawRect(x + 1, y + 1, width - 2, 1, color);
        renderer.drawRect(x, y + 2, width, height - 4, color);
        renderer.drawRect(x + 1, y + height - 2, width - 2, 1, color);
        renderer.drawRect(x + 3, y + height - 1, width - 6, 1, color);
    }

    /**
     * Draws a timeline marker (keyframe or replay marker) as a filled square with a contrasting border.
     * Selected markers get a bright border so the current selection stays visible on top of a segment.
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

    /* --------------------------------------------------------------------- utils */

    /**
     * Whether the mouse is inside {@code element}.
     *
     * <p>jGui reports the mouse in the coordinate space of the element's <em>parent</em>, so the point has to be
     * converted before it can be compared against the element's own size. Anything drawn from a panel or popup
     * that is not at the screen origin (which is most of the mod's chrome) gets a wrong answer without this.</p>
     */
    public static boolean isHovered(GuiElement element, ReadableDimension size, RenderInfo renderInfo) {
        if (!element.isEnabled()) {
            return false;
        }
        Point mouse = new Point(renderInfo.getMouseX(), renderInfo.getMouseY());
        GuiContainer<?> container = element.getContainer();
        if (container != null) {
            container.convertFor(element, mouse);
        }
        return mouse.getX() >= 0 && mouse.getY() >= 0
                && mouse.getX() < size.getWidth() && mouse.getY() < size.getHeight();
    }

    public static int fontHeight() {
        return MCVer.getFontRenderer().fontHeight;
    }

    public static int textWidth(String text) {
        return text == null ? 0 : MCVer.getFontRenderer().getWidth(text);
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

    /** Linear blend between two ARGB colors. */
    public static int mix(int from, int to, float t) {
        float clamped = Math.max(0f, Math.min(1f, t));
        int a = (int) (((from >>> 24) & 0xFF) + (((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * clamped);
        int r = (int) (((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * clamped);
        int g = (int) (((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * clamped);
        int b = (int) ((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * clamped);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
