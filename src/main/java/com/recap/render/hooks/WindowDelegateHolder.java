package com.recap.render.hooks;

import com.recap.render.gui.progress.VirtualWindow;
import net.minecraft.client.MinecraftClient;

public interface WindowDelegateHolder {
    void setWindowDelegate(VirtualWindow window);

    static WindowDelegateHolder get(MinecraftClient mc) {
        return (WindowDelegateHolder) mc;
    }
}
