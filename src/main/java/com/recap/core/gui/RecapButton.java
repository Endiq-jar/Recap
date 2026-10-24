package com.recap.core.gui;

import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.container.GuiContainer;
import de.johni0702.minecraft.gui.element.GuiButton;
import de.johni0702.minecraft.gui.function.Click;
import de.johni0702.minecraft.gui.utils.Consumer;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;

/**
 * The Recap button.
 *
 * <p>Four variants share one geometry so that any two buttons next to each other line up:</p>
 * <ul>
 *   <li>{@link Variant#PRIMARY} - the brand gradient, used for exactly one action per screen.</li>
 *   <li>{@link Variant#SECONDARY} - a quiet surface with a hairline border.</li>
 *   <li>{@link Variant#GHOST} - no background at all until hovered.</li>
 *   <li>{@link Variant#DANGER} - the destructive action (delete, discard).</li>
 * </ul>
 */
public class RecapButton extends GuiButton {

    public enum Variant {
        PRIMARY, SECONDARY, GHOST, DANGER
    }

    private Variant variant = Variant.PRIMARY;
    /** Marks this button as the currently selected entry of a navigation list. */
    private boolean active;

    public RecapButton() {
    }

    public RecapButton(GuiContainer container) {
        super(container);
    }

    public RecapButton setVariant(Variant variant) {
        this.variant = variant;
        return this;
    }

    public Variant getVariant() {
        return variant;
    }

    public RecapButton setActive(boolean active) {
        this.active = active;
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public RecapButton setPrimary(boolean primary) {
        this.variant = primary ? Variant.PRIMARY : Variant.SECONDARY;
        return this;
    }

    public boolean isPrimary() {
        return variant == Variant.PRIMARY;
    }

    public RecapButton setDanger() {
        this.variant = Variant.DANGER;
        return this;
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
        boolean hovered = RecapTheme.isHovered(this, size, renderInfo);
        int width = size.getWidth();
        int height = size.getHeight();
        int text = RecapTheme.TEXT;

        if (active) {
            // Selected navigation entry: filled row, accent rail on the left, bright label.
            RecapTheme.roundedRect(renderer, 0, 0, width, height, RecapTheme.ROW_ACTIVE);
            renderer.drawRect(0, 2, 2, height - 4, RecapTheme.ACCENT);
            String activeLabel = getLabel();
            if (activeLabel != null) {
                renderer.drawString(12, (height - RecapTheme.fontHeight()) / 2,
                        hovered ? RecapTheme.ACCENT_HOVER : RecapTheme.TEXT, activeLabel, false);
            }
            return;
        }

        switch (variant) {
            case PRIMARY: {
                if (!isEnabled()) {
                    RecapTheme.roundedRect(renderer, 0, 0, width, height, 0xFF232B3D);
                    text = RecapTheme.TEXT_FAINT;
                    break;
                }
                int top = hovered ? RecapTheme.ACCENT_HOVER : RecapTheme.ACCENT;
                int bottom = hovered ? RecapTheme.mix(RecapTheme.ACCENT_HOVER, RecapTheme.ACCENT_ALT, 0.55f)
                        : RecapTheme.ACCENT_ALT;
                RecapTheme.roundedRect(renderer, 0, 0, width, height, RecapTheme.mix(top, bottom, 0.5f));
                // Two band gradient inside the chamfer.
                renderer.drawRect(2, 1, width - 4, height / 2 - 1, top, top, bottom, bottom);
                // Bright hairline on top: the "lit edge" of the brand gradient.
                renderer.drawRect(4, 1, width - 8, 1, RecapTheme.withAlpha(0xFFFFFFFF, 0.6f));
                break;
            }
            case SECONDARY: {
                int background = !isEnabled() ? 0xFF141824
                        : hovered ? RecapTheme.ROW_ACTIVE : RecapTheme.ROW;
                RecapTheme.roundedRect(renderer, 0, 0, width, height, background);
                renderer.drawRect(1, 0, width - 2, 1, hovered ? RecapTheme.withAlpha(RecapTheme.ACCENT, 0.8f) : RecapTheme.BORDER);
                renderer.drawRect(1, height - 1, width - 2, 1, RecapTheme.BORDER);
                if (!isEnabled()) {
                    text = RecapTheme.TEXT_FAINT;
                }
                break;
            }
            case GHOST: {
                if (hovered) {
                    RecapTheme.roundedRect(renderer, 0, 0, width, height, RecapTheme.withAlpha(RecapTheme.ROW_HOVER, 0.9f));
                }
                renderer.drawRect(2, height - 1, width - 4, 1,
                        hovered ? RecapTheme.ACCENT : RecapTheme.withAlpha(RecapTheme.BORDER, 0.7f));
                text = !isEnabled() ? RecapTheme.TEXT_FAINT : (hovered ? RecapTheme.ACCENT_HOVER : RecapTheme.TEXT_DIM);
                break;
            }
            case DANGER:
            default: {
                int background = !isEnabled() ? 0xFF1B1620
                        : hovered ? RecapTheme.mix(RecapTheme.DANGER, 0xFFFFFFFF, 0.18f)
                        : RecapTheme.withAlpha(RecapTheme.DANGER, 0.22f);
                RecapTheme.roundedRect(renderer, 0, 0, width, height, background);
                renderer.drawRect(1, 0, width - 2, 1, RecapTheme.withAlpha(RecapTheme.DANGER, hovered ? 0.9f : 0.6f));
                renderer.drawRect(1, height - 1, width - 2, 1, RecapTheme.withAlpha(RecapTheme.DANGER, 0.4f));
                text = !isEnabled() ? RecapTheme.TEXT_FAINT : RecapTheme.DANGER;
                break;
            }
        }

        String label = getLabel();
        if (label != null) {
            int fontHeight = RecapTheme.fontHeight();
            renderer.drawCenteredString(width / 2, (height - fontHeight) / 2, text, label, false);
        }
    }
}
