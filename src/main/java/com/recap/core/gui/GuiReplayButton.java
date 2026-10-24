package com.recap.core.gui;

import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.element.GuiButton;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import net.minecraft.util.Identifier;

import static de.johni0702.minecraft.gui.versions.MCVer.identifier;

/**
 * The main-menu entry point.
 *
 * <p>Previously this was a stock Minecraft button with the mod logo painted over it. It now paints its own
 * surface so the first thing a player sees of Recap already uses the app's colour language: a card-toned slab,
 * a neon accent edge that lights up under the cursor and the logo inset in the middle.</p>
 */
public class GuiReplayButton extends GuiButton {
    public static final Identifier ICON = identifier("recap", "logo_button.png");

    @Override
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        int width = size.getWidth();
        int height = size.getHeight();

        if (renderInfo.getLayer() == 0) {
            boolean hovered = RecapTheme.isHovered(this, size, renderInfo);
            RecapTheme.roundedRect(renderer, 0, 0, width, height,
                    hovered ? RecapTheme.ROW_HOVER : RecapTheme.CARD);
            renderer.drawRect(0, height - 2, width, 2,
                    hovered ? RecapTheme.ACCENT : RecapTheme.withAlpha(RecapTheme.ACCENT, 0.45f));
        }

        int icon = Math.min(width, height) - 6;
        int x = (width - icon) / 2;
        int y = (height - icon) / 2;
        renderer.bindTexture(ICON);
        renderer.drawTexturedRect(x, y, 0, 0, icon, icon, 1, 1, 1, 1);
    }
}
