package com.recap.editor;

import com.recap.core.Module;
import com.recap.core.RecapMod;
import com.recap.editor.handler.GuiHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecapModEditor implements Module {
    { instance = this; }
    public static RecapModEditor instance;

    private RecapMod core;

    public static Logger LOGGER = LogManager.getLogger();

    public RecapModEditor(RecapMod core) {
        this.core = core;
    }

    @Override
    public void initClient() {
        new GuiHandler().register();
    }

    public RecapMod getCore() {
        return core;
    }
}
