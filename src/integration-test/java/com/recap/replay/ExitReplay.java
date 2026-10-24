package com.recap.replay;

import com.recap.core.AbstractTask;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;

public class ExitReplay extends AbstractTask {
    @Override
    protected void init() {
        mc.displayInGameMenu();
        expectGui(GuiIngameMenu.class, ingameMenu -> {
            click("Exit Replay");
            expectGui(GuiMainMenu.class, mainMenu -> future.set(null));
        });
    }
}
