package com.recap.render.gui;

import com.recap.core.gui.RecapCheckbox;
import com.recap.core.gui.RecapButton;
import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.recap.core.gui.RecapTheme;
import de.johni0702.minecraft.gui.container.GuiPanel;
import de.johni0702.minecraft.gui.container.GuiScreen;
import de.johni0702.minecraft.gui.element.GuiButton;
import de.johni0702.minecraft.gui.element.GuiLabel;
import de.johni0702.minecraft.gui.layout.CustomLayout;
import de.johni0702.minecraft.gui.layout.HorizontalLayout;
import de.johni0702.minecraft.gui.layout.VerticalLayout;

import java.net.URI;

import static com.recap.core.versions.MCVer.openURL;
import static de.johni0702.minecraft.gui.versions.MCVer.setClipboardString;

public class GuiNoFfmpeg extends GuiScreen {

    @Override
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        if (renderInfo.getLayer() == 0) {
            // Recap Mod: themed backdrop and a warning card instead of the vanilla dirt background.
            RecapTheme.chrome(renderer, size.getWidth(), size.getHeight(),
                    net.minecraft.client.resource.language.I18n.translate("recap.gui.rendering.error.title"),
                    net.minecraft.client.resource.language.I18n.translate("recap.gui.noffmpeg.subtitle"));
            if (cardWidth > 0) {
                RecapTheme.card(renderer, cardX, cardY, cardWidth, cardHeight);
                renderer.drawRect(cardX, cardY + 1, 2, cardHeight - 2, RecapTheme.DANGER);
                RecapTheme.chip(renderer, cardX + 18, cardY + 14, RecapTheme.textWidth("MISSING FFMPEG") + 12, 12,
                        "MISSING FFMPEG", RecapTheme.DANGER);
            }
        }
        super.draw(renderer, size, renderInfo);
    }


    private static final String LINK = "https://github.com/Endiq-jar/Recap#ffmpeg-install-scripts";

    private final GuiLabel message = new GuiLabel()
            .setI18nText("recap.gui.rendering.error.message")
            .setColor(RecapTheme.TEXT_COLOR);
    private final GuiLabel link = new GuiLabel()
            .setText(LINK)
            .setColor(RecapTheme.ACCENT_COLOR);
    private final GuiButton openLinkButton = new RecapButton().setVariant(RecapButton.Variant.SECONDARY)
            .setI18nLabel("chat.link.open")
            .setSize(100, 20)
            .onClick(() -> openURL(URI.create(LINK)));
    private final GuiButton copyToClipboardButton = new RecapButton().setVariant(RecapButton.Variant.SECONDARY)
            .setI18nLabel("chat.copy")
            .setSize(100, 20)
            .onClick(() -> setClipboardString(LINK));
    private final GuiButton backButton = new RecapButton().setVariant(RecapButton.Variant.GHOST)
            .setI18nLabel("gui.back")
            .setSize(100, 20);
    private final GuiPanel buttons = new GuiPanel()
            .setLayout(new HorizontalLayout(HorizontalLayout.Alignment.CENTER).setSpacing(4))
            .addElements(null, openLinkButton, copyToClipboardButton, backButton);

    /** Bounds of the card drawn behind the content, computed during layout. */
    private int cardX, cardY, cardWidth, cardHeight;

    {
        setBackground(Background.NONE);
        final GuiPanel content = new GuiPanel(this)
                .setLayout(new VerticalLayout(VerticalLayout.Alignment.CENTER).setSpacing(10))
                .addElements(new VerticalLayout.Data(0.5), message, link, buttons);
        setLayout(new CustomLayout<GuiScreen>() {
            @Override
            protected void layout(GuiScreen container, int width, int height) {
                cardWidth = Math.max(380, width(content) + 64);
                cardHeight = height(content) + 56;
                cardX = (width - cardWidth) / 2;
                cardY = (height - cardHeight) / 2;
                pos(content, width / 2 - width(content) / 2, cardY + 28);
            }
        });
        addElements(null, content);
    }

    public GuiNoFfmpeg(Runnable goBack) {
        backButton.onClick(goBack);
    }
}
