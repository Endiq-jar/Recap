package com.recap.recording.gui;

import com.recap.core.RecapMod;
import com.recap.core.utils.Utils;
import com.recap.core.versions.MCVer;
import com.recap.editor.gui.MarkerProcessor;
import com.recap.recording.packet.PacketListener;
import de.johni0702.minecraft.gui.container.GuiPanel;
import de.johni0702.minecraft.gui.container.VanillaGuiScreen;
import com.recap.core.gui.RecapButton;
import de.johni0702.minecraft.gui.element.GuiButton;
import de.johni0702.minecraft.gui.layout.CustomLayout;
import de.johni0702.minecraft.gui.layout.HorizontalLayout;
import de.johni0702.minecraft.gui.utils.EventRegistrations;
import de.johni0702.minecraft.gui.versions.callbacks.InitScreenCallback;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;

//#if MC>=11400
import net.minecraft.client.gui.widget.AbstractButtonWidget;
//#endif

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class GuiRecordingControls extends EventRegistrations {
    private RecapMod core;
    private PacketListener packetListener;
    private boolean paused;
    private boolean stopped;

    private GuiPanel panel = new GuiPanel().setBackgroundColor(com.recap.core.gui.RecapTheme.HUD_COLOR)
            .setLayout(new HorizontalLayout().setSpacing(4));

    private GuiButton buttonPauseResume = new RecapButton(panel).setVariant(RecapButton.Variant.SECONDARY).onClick(() -> {
        if (Utils.ifMinimalModeDoPopup(panel, () -> {})) return;
        if (paused) {
            packetListener.addMarker(MarkerProcessor.MARKER_NAME_END_CUT);
        } else {
            packetListener.addMarker(MarkerProcessor.MARKER_NAME_START_CUT);
        }
        paused = !paused;
        updateState();
    }).setSize(98, 20);

    private GuiButton buttonStartStop = new RecapButton(panel).setVariant(RecapButton.Variant.PRIMARY).onClick(() -> {
        if (Utils.ifMinimalModeDoPopup(panel, () -> {})) return;
        if (stopped) {
            paused = false;
            packetListener.addMarker(MarkerProcessor.MARKER_NAME_END_CUT);
            core.printInfoToChat("recap.chat.recordingstarted");
        } else {
            int timestamp = (int) packetListener.getCurrentDuration();
            if (!paused) {
                packetListener.addMarker(MarkerProcessor.MARKER_NAME_START_CUT, timestamp);
            }
            packetListener.addMarker(MarkerProcessor.MARKER_NAME_SPLIT, timestamp + 1);
        }
        stopped = !stopped;
        updateState();
    }).setSize(98, 20);

    public GuiRecordingControls(RecapMod core, PacketListener packetListener, boolean autoStart) {
        this.core = core;
        this.packetListener = packetListener;

        paused = stopped = !autoStart;

        updateState();
    }

    private void updateState() {
        buttonPauseResume.setI18nLabel("recap.gui.recording." + (paused ? "resume" : "pause"));
        buttonStartStop.setI18nLabel("recap.gui.recording." + (stopped ? "start" : "stop"));

        buttonPauseResume.setEnabled(!stopped);
    }

    { on(InitScreenCallback.EVENT, this::injectIntoIngameMenu); }
    private void injectIntoIngameMenu(Screen guiScreen,
                                      //#if MC>=11400
                                      Collection<AbstractButtonWidget> buttonList
                                      //#else
                                      //$$ Collection<net.minecraft.client.gui.GuiButton> buttonList
                                      //#endif
    ) {
        if (!(guiScreen instanceof GameMenuScreen)) {
            return;
        }
        if (buttonList.isEmpty()) {
            return; // menu-less pause (F3+Esc)
        }
        Function<Integer, Integer> yPos =
                MCVer.findButton(buttonList, "menu.returnToMenu", 1)
                        .map(Optional::of)
                        .orElse(MCVer.findButton(buttonList, "menu.disconnect", 1))
                        .<Function<Integer, Integer>>map(it -> (height) -> it.y)
                        .orElse((height) -> height / 4 + 120 - 16);
        VanillaGuiScreen vanillaGui = VanillaGuiScreen.wrap(guiScreen);
        vanillaGui.setLayout(new CustomLayout<de.johni0702.minecraft.gui.container.GuiScreen>(vanillaGui.getLayout()) {
            @Override
            protected void layout(de.johni0702.minecraft.gui.container.GuiScreen container, int width, int height) {
                pos(panel, width / 2 - 100, yPos.apply(height) + 16 + 8);
            }
        }).addElements(null, panel);
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isStopped() {
        return stopped;
    }
}
