package com.recap.render.gui;

import com.recap.core.gui.RecapCheckbox;
import com.recap.core.gui.RecapButton;
import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import com.recap.core.gui.RecapTheme;
import com.recap.core.RecapMod;
import com.recap.render.RenderSettings;
import com.recap.render.FFmpegWriter;
import de.johni0702.minecraft.gui.container.GuiPanel;
import de.johni0702.minecraft.gui.container.GuiScreen;
import de.johni0702.minecraft.gui.container.GuiVerticalList;
import de.johni0702.minecraft.gui.element.GuiButton;
import de.johni0702.minecraft.gui.element.GuiElement;
import de.johni0702.minecraft.gui.element.GuiLabel;
import de.johni0702.minecraft.gui.layout.CustomLayout;
import de.johni0702.minecraft.gui.layout.HorizontalLayout;
import de.johni0702.minecraft.gui.layout.VerticalLayout;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.crash.CrashException;

import java.util.Arrays;
import java.util.function.Consumer;

import static com.recap.render.RecapModRender.LOGGER;

public class GuiExportFailed extends GuiScreen {

    @Override
    public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
        if (renderInfo.getLayer() == 0) {
            // Recap Mod: themed backdrop instead of the vanilla dirt background.
            RecapTheme.chrome(renderer, size.getWidth(), size.getHeight(),
                    net.minecraft.client.resource.language.I18n.translate("recap.gui.rendering.error.title"),
                    net.minecraft.client.resource.language.I18n.translate("recap.gui.rendering.error.subtitle"));
            String badge = "FFMPEG FAILED";
            int badgeWidth = RecapTheme.textWidth(badge) + 14;
            RecapTheme.chip(renderer, size.getWidth() - 18 - badgeWidth, 46, badgeWidth, 14, badge, RecapTheme.DANGER);
            if (logSlabY > 0) {
                // The FFmpeg log sits on its own dark slab so it reads as a console rather than as body text.
                RecapTheme.roundedRect(renderer, 14, logSlabY, size.getWidth() - 28, logSlabHeight, 0xFF07080F);
                renderer.drawRect(14, logSlabY + 1, 2, logSlabHeight - 2, RecapTheme.DANGER);
            }
        }
        super.draw(renderer, size, renderInfo);
    }

    public static GuiExportFailed tryToRecover(FFmpegWriter.FFmpegStartupException e, Consumer<RenderSettings> doRestart) {
        // Always log the error first
        LOGGER.error("Rendering video:", e);

        RenderSettings settings = e.getSettings();
        // Check whether the user has configured some custom ffmpeg arguments
        if (settings.getEncodingPreset().getValue().equals(settings.getExportArguments())) {
            // If they haven't, then this is probably a faulty ffmpeg installation and there's nothing we can do
            CrashReport crashReport = CrashReport.create(e, "Exporting video");
            CrashReportSection details = crashReport.addElement("Export details");
            details.add("Settings", settings::toString);
            details.add("FFmpeg log", e::getLog);
            throw new CrashException(crashReport);
        } else {
            // If they have, ask them whether it was intentional
            GuiExportFailed gui = new GuiExportFailed(e, doRestart);
            gui.display();
            return gui;
        }
    }

    private int logSlabY, logSlabHeight;

    private final GuiLabel logLabel = new GuiLabel(this)
            .setI18nText("recap.gui.rendering.error.ffmpeglog")
            .setColor(RecapTheme.ACCENT_COLOR);
    private final GuiVerticalList logList = new GuiVerticalList(this).setDrawShadow(true);
    private final GuiButton resetButton = new RecapButton().setI18nLabel("gui.yes").setSize(100, 20);
    private final GuiButton abortButton = new RecapButton().setVariant(RecapButton.Variant.GHOST).setI18nLabel("gui.no").setSize(100, 20);
    private final GuiPanel info = new GuiPanel(this)
            .setLayout(new VerticalLayout().setSpacing(4))
            .addElements(new VerticalLayout.Data(0.5),
                    new GuiLabel().setI18nText("recap.gui.rendering.error.ffmpegargs.1")
                            .setColor(RecapTheme.TEXT_COLOR),
                    new GuiLabel().setI18nText("recap.gui.rendering.error.ffmpegargs.2")
                            .setColor(RecapTheme.TEXT_DIM_COLOR),
                    new GuiLabel(),
                    new GuiPanel().setLayout(new HorizontalLayout(HorizontalLayout.Alignment.CENTER).setSpacing(5))
                            .addElements(null, resetButton, abortButton)
            );

    {
        setLayout(new CustomLayout<GuiScreen>() {
            @Override
            protected void layout(GuiScreen container, int width, int height) {
                pos(info, width / 2 - width(info) / 2, 88);
                pos(logLabel, width / 2 - width(logLabel) / 2, y(info) + height(info) + 14);
                pos(logList, 26, y(logLabel) + height(logLabel) + 10);
                size(logList, width - 44, height - 52 - y(logList));
                logSlabY = y(logLabel) - 4;
                logSlabHeight = height - 10 - logSlabY;
            }
        });
        setBackground(Background.NONE);
    }

    public GuiExportFailed(FFmpegWriter.FFmpegStartupException e, Consumer<RenderSettings> doRestart) {
        logList.getListPanel().addElements(null,
                Arrays.stream(e.getLog().replace("\t", "    ").split("\n"))
                        .map(l -> new GuiLabel().setText(l))
                        .toArray(GuiElement[]::new));

        resetButton.onClick(() -> RecapMod.instance.runLater(() -> {
            RenderSettings oldSettings = e.getSettings();
            doRestart.accept(new RenderSettings(
                    oldSettings.getRenderMethod(),
                    oldSettings.getEncodingPreset(),
                    oldSettings.getVideoWidth(),
                    oldSettings.getVideoHeight(),
                    oldSettings.getFramesPerSecond(),
                    oldSettings.getBitRate(),
                    oldSettings.getOutputFile(),
                    oldSettings.isRenderNameTags(),
                    oldSettings.isIncludeAlphaChannel(),
                    oldSettings.isStabilizeYaw(),
                    oldSettings.isStabilizePitch(),
                    oldSettings.isStabilizeRoll(),
                    oldSettings.getChromaKeyingColor(),
                    oldSettings.getSphericalFovX(),
                    oldSettings.getSphericalFovY(),
                    oldSettings.isInjectSphericalMetadata(),
                    oldSettings.isDepthMap(),
                    oldSettings.isCameraPathExport(),
                    oldSettings.getAntiAliasing(),
                    oldSettings.getExportCommand(),
                    oldSettings.getEncodingPreset().getValue(),
                    oldSettings.isHighPerformance()
            ));
        }));

        abortButton.onClick(() -> {
            // Assume they know what they're doing
            getMinecraft().openScreen(null);
        });
    }
}
