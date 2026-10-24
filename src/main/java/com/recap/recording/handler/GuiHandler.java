package com.recap.recording.handler;

import com.recap.core.RecapMod;
import com.recap.core.SettingsRegistry;
import com.recap.core.gui.GuiReplayButton;
import com.recap.core.gui.RecapCheckbox;
import com.recap.recording.ServerInfoExt;
import com.recap.recording.Setting;
import com.recap.recording.mixin.AddServerScreenAccessor;
import de.johni0702.minecraft.gui.container.GuiScreen;
import de.johni0702.minecraft.gui.container.VanillaGuiScreen;
import de.johni0702.minecraft.gui.element.GuiButton;
import de.johni0702.minecraft.gui.element.GuiCheckbox;
import de.johni0702.minecraft.gui.element.GuiToggleButton;
import de.johni0702.minecraft.gui.layout.CustomLayout;
import de.johni0702.minecraft.gui.popup.GuiInfoPopup;
import de.johni0702.minecraft.gui.utils.EventRegistrations;
import de.johni0702.minecraft.gui.versions.callbacks.InitScreenCallback;
import net.minecraft.client.gui.screen.AddServerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.resource.language.I18n;

public class GuiHandler extends EventRegistrations {

    private final RecapMod mod;

    public GuiHandler(RecapMod mod) {
        this.mod = mod;
    }

    { on(InitScreenCallback.EVENT, (screen, buttons) -> onGuiInit(screen)); }
    private void onGuiInit(net.minecraft.client.gui.screen.Screen gui) {
        if (gui instanceof SelectWorldScreen || gui instanceof MultiplayerScreen) {
            boolean sp = gui instanceof SelectWorldScreen;
            SettingsRegistry settingsRegistry = mod.getSettingsRegistry();
            Setting<Boolean> setting = sp ? Setting.RECORD_SINGLEPLAYER : Setting.RECORD_SERVER;

            GuiCheckbox recordingCheckbox = new RecapCheckbox()
                    .setI18nLabel("recap.gui.settings.record" + (sp ? "singleplayer" : "server"))
                    .setChecked(settingsRegistry.get(setting));
            recordingCheckbox.onClick(() -> {
                settingsRegistry.set(setting, recordingCheckbox.isChecked());
                settingsRegistry.save();
            });

            VanillaGuiScreen vanillaGui = VanillaGuiScreen.wrap(gui);
            vanillaGui.setLayout(new CustomLayout<GuiScreen>(vanillaGui.getLayout()) {
                @Override
                protected void layout(GuiScreen container, int width, int height) {
                    //size(recordingCheckbox, 200, 20);
                    pos(recordingCheckbox, width - width(recordingCheckbox) - 5, 5);
                }
            }).addElements(null, recordingCheckbox);
        }

        if (gui instanceof AddServerScreen) {
            VanillaGuiScreen vanillaGui = VanillaGuiScreen.wrap(gui);
            GuiButton replayButton = new GuiReplayButton().onClick(() -> {
                ServerInfo serverInfo = ((AddServerScreenAccessor) gui).getServer();
                ServerInfoExt serverInfoExt = ServerInfoExt.from(serverInfo);
                Boolean state = serverInfoExt.getAutoRecording();
                GuiToggleButton<String> autoRecording = new GuiToggleButton<String>()
                        .setI18nLabel("recap.gui.settings.autostartrecording")
                        .setValues(
                                I18n.translate("recap.gui.settings.default"),
                                I18n.translate("options.off"),
                                I18n.translate("options.on")
                        )
                        .setSelected(state == null ? 0 : state ? 2 : 1);
                autoRecording.onClick(() -> {
                    int selected = autoRecording.getSelected();
                    serverInfoExt.setAutoRecording(selected == 0 ? null : selected == 2);
                });
                GuiInfoPopup.open(vanillaGui, autoRecording);
            });
            vanillaGui.setLayout(new CustomLayout<GuiScreen>(vanillaGui.getLayout()) {
                @Override
                protected void layout(GuiScreen container, int width, int height) {
                    size(replayButton, 20, 20);
                    pos(replayButton, width - width(replayButton) - 5, 5);
                }
            }).addElements(null, replayButton);
        }
    }
}
