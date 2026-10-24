package com.recap.extras.advancedscreenshots;

import com.recap.core.gui.RecapTheme;
import com.recap.core.gui.RecapCheckbox;
import com.recap.core.gui.RecapButton;
import com.recap.core.RecapMod;
import com.recap.core.SettingsRegistry;
import com.recap.core.versions.MCVer;
import com.recap.extras.Setting;
import com.recap.render.RenderSettings;
import de.johni0702.minecraft.gui.container.GuiContainer;
import de.johni0702.minecraft.gui.container.GuiPanel;
import de.johni0702.minecraft.gui.element.GuiButton;
import de.johni0702.minecraft.gui.element.GuiCheckbox;
import de.johni0702.minecraft.gui.element.GuiLabel;
import de.johni0702.minecraft.gui.layout.HorizontalLayout;
import de.johni0702.minecraft.gui.layout.VerticalLayout;
import de.johni0702.minecraft.gui.popup.AbstractGuiPopup;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableColor;

import java.net.URI;

public class GuiUploadScreenshot extends AbstractGuiPopup<GuiUploadScreenshot> {

    public final RecapMod mod;

    public final RenderSettings renderSettings;

    public final GuiLabel successLabel = new GuiLabel()
            .setI18nText("recap.gui.advancedscreenshots.finished.description")
            .setColor(RecapTheme.TEXT_COLOR);

    public final GuiLabel veerLabel = new GuiLabel()
            .setI18nText("recap.gui.advancedscreenshots.finished.description.veer")
            .setColor(RecapTheme.TEXT_COLOR);

    public final GuiButton veerUploadButton = new RecapButton().setVariant(RecapButton.Variant.SECONDARY)
            .setSize(150, 20)
            .setI18nLabel("recap.gui.advancedscreenshots.finished.upload.veer");

    public final GuiButton showOnDiskButton = new RecapButton().setVariant(RecapButton.Variant.SECONDARY)
            .setSize(150, 20)
            .setI18nLabel("recap.gui.advancedscreenshots.finished.showfile");

    public final GuiButton closeButton = new RecapButton()
            .setSize(150, 20)
            .setI18nLabel("recap.gui.close");

    public final GuiCheckbox neverOpenCheckbox = new RecapCheckbox();

    public final GuiLabel neverOpenLabel = new GuiLabel()
            .setI18nText("recap.gui.notagain")
            .setColor(RecapTheme.TEXT_COLOR);

    public final GuiPanel checkboxPanel = GuiPanel.builder()
            .layout(new HorizontalLayout(HorizontalLayout.Alignment.RIGHT).setSpacing(5))
            .with(neverOpenCheckbox, new HorizontalLayout.Data(0.5))
            .with(neverOpenLabel, new HorizontalLayout.Data(0.5))
            .build();

    public GuiUploadScreenshot(GuiContainer container, RecapMod mod, RenderSettings renderSettings) {
        super(container);
        this.mod = mod;
        this.renderSettings = renderSettings;

        boolean veer = renderSettings.getRenderMethod() == RenderSettings.RenderMethod.EQUIRECTANGULAR;

        if (renderSettings.getRenderMethod() == RenderSettings.RenderMethod.EQUIRECTANGULAR) {
            successLabel.setI18nText("recap.gui.advancedscreenshots.finished.description.360");
        }

        if (veer) {
            veerUploadButton.onClick(() -> MCVer.openURL(URI.create("https://veer.tv/upload")));
        }

        showOnDiskButton.onClick(() -> MCVer.openFile(renderSettings.getOutputFile().getParentFile()));

        closeButton.onClick(() -> {
            if (neverOpenCheckbox.isChecked()) {
                SettingsRegistry settingsRegistry = mod.getSettingsRegistry();
                settingsRegistry.set(Setting.SKIP_POST_SCREENSHOT_GUI, true);
                settingsRegistry.save();
            }
            close();
        });

        popup.addElements(new VerticalLayout.Data(0.5), successLabel);

        if (veer) {
            popup.addElements(new VerticalLayout.Data(0.5),
                    veerLabel,
                    veerUploadButton);
        }

        popup.addElements(new VerticalLayout.Data(0.5),
                successLabel,
                showOnDiskButton,
                closeButton);

        popup.addElements(new VerticalLayout.Data(1),
                checkboxPanel);

        popup.setLayout(new VerticalLayout().setSpacing(5));
    }

    @Override
    protected void open() {
        if (mod.getSettingsRegistry().get(Setting.SKIP_POST_SCREENSHOT_GUI)) {
            return;
        }
        super.open();
    }

    @Override
    protected GuiUploadScreenshot getThis() {
        return this;
    }
}
