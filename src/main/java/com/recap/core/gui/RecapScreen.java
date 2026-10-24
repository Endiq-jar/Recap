package com.recap.core.gui;

import com.recap.core.RecapMod;
import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.container.AbstractGuiScreen;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;

/**
 * Base class for every Recap Mod screen.
 *
 * <p>It replaces the vanilla blur/dirt background with the Recap theme and renders the shared chrome: the
 * brand top bar (with the mod version), the title block and an optional footer hint. Screens therefore only
 * have to worry about their own content.</p>
 */
public abstract class RecapScreen<T extends AbstractGuiScreen<T>> extends AbstractGuiScreen<T> {

    /** Y coordinate where screen content may start below the chrome. */
    protected static final int CONTENT_TOP = 78;

    /** Vertical space reserved at the bottom of the screen for the footer. */
    protected static final int FOOTER_SPACE = 46;

    private String headerTitle;
    private String headerSubtitle;
    private String footerHint;

    protected RecapScreen() {
        setBackground(Background.NONE);
    }

    protected void setHeader(String title, String subtitle) {
        this.headerTitle = title;
        this.headerSubtitle = subtitle;
    }

    protected void setFooterHint(String footerHint) {
        this.footerHint = footerHint;
    }

    /**
     * Only ever returns the mod version; the wrapper keeps this safe on the very first frames where
     * the mod might not have finished initializing.
     */
    protected String currentVersion() {
        try {
            RecapMod mod = RecapMod.instance;
            return mod == null ? null : "v" + mod.getVersion();
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** Screens with dynamic titles can change them after construction. */
    protected void setHeaderTitle(String title, String subtitle) {
        setHeader(title, subtitle);
    }

    @Override
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        if (renderInfo.getLayer() == 0) {
            RecapTheme.screen(renderer, size.getWidth(), size.getHeight());
            RecapTheme.topBar(renderer, size.getWidth(), currentVersion());
            RecapTheme.titleBlock(renderer, size.getWidth(), headerTitle, headerSubtitle);
            if (footerHint != null) {
                RecapTheme.footer(renderer, size.getWidth(), size.getHeight(), footerHint);
            }
        }
        super.draw(renderer, size, renderInfo);
    }
}
