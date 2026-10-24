package com.recap.online;

import com.recap.core.AbstractTask;
import com.recap.online.gui.GuiLoginPrompt;

public class SkipLogin extends AbstractTask {
    @Override
    protected void init() {
        expectGui(GuiLoginPrompt.class, gui -> {
            click(gui.cancelButton);
            expectGuiClosed(() -> future.set(null));
        });
    }
}
