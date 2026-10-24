package com.recap.render.gui;

import com.recap.core.gui.RecapCheckbox;
import com.recap.core.gui.RecapButton;
import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.recap.core.gui.RecapTheme;
import com.recap.core.SettingsRegistry;
import com.recap.core.versions.MCVer;
import com.recap.render.RenderSettings;
import com.recap.render.RecapModRender;
import com.recap.render.Setting;
import de.johni0702.minecraft.gui.container.GuiPanel;
import de.johni0702.minecraft.gui.container.GuiScreen;
import de.johni0702.minecraft.gui.element.GuiButton;
import de.johni0702.minecraft.gui.element.GuiCheckbox;
import de.johni0702.minecraft.gui.element.GuiLabel;
import de.johni0702.minecraft.gui.layout.CustomLayout;
import de.johni0702.minecraft.gui.layout.HorizontalLayout;
import de.johni0702.minecraft.gui.layout.VerticalLayout;

import java.io.File;

public class GuiRenderingDone extends GuiScreen {

    @Override
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        if (renderInfo.getLayer() == 0) {
            // Recap Mod: themed backdrop with a success card instead of the vanilla dirt background.
            RecapTheme.chrome(renderer, size.getWidth(), size.getHeight(),
                    net.minecraft.client.resource.language.I18n.translate("recap.gui.renderdonetitle"),
                    net.minecraft.client.resource.language.I18n.translate("recap.gui.renderdone.subtitle"));
            if (cardWidth > 0) {
                RecapTheme.card(renderer, cardX, cardY, cardWidth, cardHeight);
                renderer.drawRect(cardX, cardY + 1, 2, cardHeight - 2, RecapTheme.SUCCESS);
                String badge = "DONE";
                int badgeWidth = RecapTheme.textWidth(badge) + 14;
                RecapTheme.chip(renderer, cardX + 24, cardY + 20, badgeWidth, 14, badge, RecapTheme.SUCCESS);
                renderer.drawString(cardX + 24, cardY + 42,
                        RecapTheme.TEXT_DIM,
                        net.minecraft.client.resource.language.I18n.translate("recap.gui.renderdone.filename")
                                + "  " + videoFile.getName());
            }
        }
        super.draw(renderer, size, renderInfo);
    }

    public final RecapModRender mod;
    public final File videoFile;
    public final int videoFrames;
    public final RenderSettings settings;

    public final GuiLabel infoLine1 = new GuiLabel().setI18nText("recap.gui.renderdone1")
            .setColor(RecapTheme.TEXT_COLOR);
    public final GuiLabel infoLine2 = new GuiLabel().setI18nText("recap.gui.renderdone2")
            .setColor(RecapTheme.TEXT_DIM_COLOR);

    public final GuiButton openFolder = new RecapButton().onClick(new Runnable() {
        @Override
        public void run() {
            MCVer.openFile(videoFile.getParentFile());
        }
    }).setSize(200, 20).setI18nLabel("recap.gui.openfolder");

    public final GuiPanel actionsPanel = new GuiPanel().setLayout(new VerticalLayout().setSpacing(10))
            .addElements(null, openFolder);

    public final GuiPanel mainPanel = new GuiPanel(this).setLayout(new VerticalLayout().setSpacing(10))
            .addElements(new VerticalLayout.Data(0.5),
                    new GuiPanel().setLayout(new VerticalLayout().setSpacing(4)).addElements(null, infoLine1, infoLine2),
                    actionsPanel);

    public final GuiButton closeButton = new RecapButton().setVariant(RecapButton.Variant.GHOST).onClick(new Runnable() {
        @Override
        public void run() {
            if (neverOpenCheckbox.isChecked()) {
                SettingsRegistry settingsRegistry = mod.getCore().getSettingsRegistry();
                settingsRegistry.set(Setting.SKIP_POST_RENDER_GUI, true);
                settingsRegistry.save();
            }
            getMinecraft().openScreen(null);
        }
    }).setSize(100, 20).setI18nLabel("recap.gui.close");

    public final GuiCheckbox neverOpenCheckbox = new RecapCheckbox().setI18nLabel("recap.gui.notagain");

    public final GuiPanel closePanel = new GuiPanel(this)
            .setLayout(new HorizontalLayout(HorizontalLayout.Alignment.RIGHT).setSpacing(5))
            .addElements(new HorizontalLayout.Data(0.5), neverOpenCheckbox, closeButton);

    /** Bounds of the success card, computed during layout. */
    private int cardX, cardY, cardWidth, cardHeight;

    {
        setLayout(new CustomLayout<GuiScreen>() {
            @Override
            protected void layout(GuiScreen container, int width, int height) {
                // The card is the screen: one centred panel holding the confirmation and the actions.
                pos(closePanel, 18, height - 14 - height(closePanel));
                cardWidth = Math.max(420, width(mainPanel) + 96);
                cardHeight = height(mainPanel) + 84;
                cardX = (width - cardWidth) / 2;
                cardY = Math.max(88, height / 2 - cardHeight / 2 - 10);
                pos(mainPanel, width / 2 - width(mainPanel) / 2, cardY + 58);
            }
        });
        setBackground(Background.NONE);
    }

    public GuiRenderingDone(RecapModRender mod, File videoFile, int videoFrames, RenderSettings settings) {
        this.mod = mod;
        this.videoFile = videoFile;
        this.videoFrames = videoFrames;
        this.settings = settings;
    }

    @Override
    public void display() {
        if (mod.getCore().getSettingsRegistry().get(Setting.SKIP_POST_RENDER_GUI)) {
            return;
        }
        super.display();
    }
}
