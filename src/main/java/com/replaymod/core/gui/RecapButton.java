package com.replaymod.core.gui;

import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.container.GuiContainer;
import de.johni0702.minecraft.gui.element.GuiButton;
import de.johni0702.minecraft.gui.function.Click;
import de.johni0702.minecraft.gui.utils.Consumer;
import de.johni0702.minecraft.gui.utils.lwjgl.Point;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import de.johni0702.minecraft.gui.versions.MCVer;

/**
 * A flat, themed button.
 *
 * <p>Primary buttons are filled with the accent colour and are used for the confirming action of a
 * screen; secondary buttons are a quiet surface with a hairline border. All the inherited fluent
 * setters are overridden to return {@code RecapButton} so call sites can keep chaining.</p>
 */
public class RecapButton extends GuiButton {

    private boolean primary = true;

    public RecapButton() {
    }

    public RecapButton(GuiContainer container) {
        super(container);
    }

    public RecapButton setPrimary(boolean primary) {
        this.primary = primary;
        return this;
    }

    public boolean isPrimary() {
        return primary;
    }

    @Override
    public RecapButton setSize(int width, int height) {
        super.setSize(width, height);
        return this;
    }

    @Override
    public RecapButton setLabel(String label) {
        super.setLabel(label);
        return this;
    }

    @Override
    public RecapButton setI18nLabel(String label, Object... args) {
        super.setI18nLabel(label, args);
        return this;
    }

    @Override
    public RecapButton onClick(Runnable onClick) {
        super.onClick(onClick);
        return this;
    }

    @Override
    public RecapButton onClick(Consumer<Click> onClick) {
        super.onClick(onClick);
        return this;
    }

    @Override
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        boolean hovered = isEnabled() && isMouseHovering(new Point(renderInfo.getMouseX(), renderInfo.getMouseY()));
        int width = size.getWidth();
        int height = size.getHeight();

        int background;
        int foreground;
        if (!isEnabled()) {
            background = 0xFF1A2029;
            foreground = RecapTheme.TEXT_DIM;
        } else if (primary) {
            background = hovered ? RecapTheme.ACCENT_HOVER : RecapTheme.ACCENT;
            foreground = RecapTheme.TEXT_ON_ACCENT;
        } else {
            background = hovered ? 0xFF2A3444 : 0xFF1E2733;
            foreground = RecapTheme.TEXT;
        }

        RecapTheme.roundedRect(renderer, 0, 0, width, height, background);
        if (!primary) {
            renderer.drawRect(0, 0, width, 1, RecapTheme.BORDER);
            renderer.drawRect(0, height - 1, width, 1, RecapTheme.BORDER);
            renderer.drawRect(0, 1, 1, height - 2, RecapTheme.BORDER);
            renderer.drawRect(width - 1, 1, 1, height - 2, RecapTheme.BORDER);
        }

        String label = getLabel();
        if (label != null) {
            int fontHeight = MCVer.getFontRenderer().fontHeight;
            renderer.drawCenteredString(width / 2, (height - fontHeight) / 2, foreground, label);
        }
    }
}
