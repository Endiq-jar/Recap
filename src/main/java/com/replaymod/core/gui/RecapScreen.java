package com.replaymod.core.gui;

import com.replaymod.core.ReplayMod;
import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.container.AbstractGuiScreen;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;

/**
 * Base class for every Recap Mod screen.
 *
 * <p>It replaces the vanilla blur/dirt background with the Recap theme, renders the header block and
 * (optionally) a footer hint, so that all screens share the same chrome.</p>
 */
public abstract class RecapScreen<T extends AbstractGuiScreen<T>> extends AbstractGuiScreen<T> {

    private String headerTitle;
    private String headerSubtitle;
    private String footerHint;
    private String badge = "RECAP";

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

    protected void setBadge(String badge) {
        this.badge = badge;
    }

    /**
     * Only ever returns the mod version; the wrapper keeps this safe on the very first frames where
     * the mod might not have finished initializing.
     */
    protected String currentVersion() {
        try {
            ReplayMod mod = ReplayMod.instance;
            return mod == null ? null : mod.getVersion();
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Override
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        if (renderInfo.getLayer() == 0) {
            RecapTheme.screen(renderer, size.getWidth(), size.getHeight());
            String version = currentVersion();
            RecapTheme.header(renderer, size.getWidth(), headerTitle, headerSubtitle,
                    version == null ? badge : badge + " " + version);
            if (footerHint != null) {
                RecapTheme.footer(renderer, size.getWidth(), size.getHeight(), footerHint);
            }
        }
        super.draw(renderer, size, renderInfo);
    }
}
