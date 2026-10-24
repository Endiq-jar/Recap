package com.recap.core.gui;

import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.container.GuiContainer;
import de.johni0702.minecraft.gui.element.GuiButton;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadablePoint;
import net.minecraft.util.Identifier;

/**
 * A themed button which still renders its legacy sprite on top.
 *
 * <p>Some of the older in-game controls (camera path playback, keyframe tools, timeline zoom) select their
 * icon through a sprite UV supplier that is updated at runtime. This class keeps that mechanism intact so
 * the behaviour is unchanged, but replaces the vanilla button chrome with the Recap surface and insets the
 * icon so it reads as a glyph on a panel instead of a stretched texture.</p>
 */
public class RecapSpriteButton extends GuiButton {

    public RecapSpriteButton() {
    }

    public RecapSpriteButton(GuiContainer container) {
        super(container);
    }

    @Override
    public RecapSpriteButton setSize(int width, int height) {
        super.setSize(width, height);
        return this;
    }

    @Override
    public RecapSpriteButton onClick(Runnable onClick) {
        super.onClick(onClick);
        return this;
    }

    @Override
    public RecapSpriteButton setTexture(Identifier texture) {
        super.setTexture(texture);
        return this;
    }

    @Override
    public RecapSpriteButton setSpriteUV(ReadablePoint spriteUV) {
        super.setSpriteUV(spriteUV);
        return this;
    }

    @Override
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        int width = size.getWidth();
        int height = size.getHeight();

        if (renderInfo.getLayer() == 0) {
            boolean hovered = RecapTheme.isHovered(this, size, renderInfo);
            int background = !isEnabled() ? 0xFF161B27 : hovered ? RecapTheme.ROW_ACTIVE : 0xFF1D2331;
            RecapTheme.roundedRect(renderer, 0, 0, width, height, background);
            renderer.drawRect(1, 0, Math.max(0, width - 2), 1, hovered ? RecapTheme.ACCENT : RecapTheme.BORDER);
            renderer.drawRect(1, height - 1, Math.max(0, width - 2), 1, RecapTheme.BORDER);
        }

        Identifier texture = getTexture();
        if (texture != null) {
            renderer.bindTexture(texture);
            ReadablePoint uv = getSpriteUV();
            ReadableDimension textureSize = getTextureSize();
            if (uv != null && textureSize != null) {
                // Keep the sprite square and centred so the themed chrome stays visible around it.
                int icon = Math.max(4, Math.min(width, height) - 6);
                int x = (width - icon) / 2;
                int y = (height - icon) / 2;
                ReadableDimension spriteSize = getSpriteSize() != null ? getSpriteSize() : getMinSize();
                renderer.drawTexturedRect(x, y, uv.getX(), uv.getY(), icon, icon,
                        spriteSize.getWidth(), spriteSize.getHeight(),
                        textureSize.getWidth(), textureSize.getHeight());
            } else {
                renderer.drawTexturedRect(0, 0, 0, 0, width, height);
            }
        }

        String label = getLabel();
        if (label != null) {
            int color = !isEnabled() ? RecapTheme.TEXT_FAINT : RecapTheme.isHovered(this, size, renderInfo) ? RecapTheme.TEXT : RecapTheme.TEXT_DIM;
            renderer.drawCenteredString(width / 2, (height - RecapTheme.fontHeight()) / 2, color, label, true);
        }
    }
}
