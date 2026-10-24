package com.recap.replay;

import com.recap.core.AbstractTask;
import com.recap.replay.gui.screen.GuiReplayViewer;
import net.minecraft.client.gui.GuiMainMenu;

public class OpenReplayViewer extends AbstractTask {
    @Override
    protected void init() {
        expectGui(GuiMainMenu.class, mainMenu -> {
            click("Replay Viewer");
            expectGui(GuiReplayViewer.class, replayViewer -> future.set(null));
        });
    }
}
