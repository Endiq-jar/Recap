package com.recap.replay.overlay;

import com.recap.core.AbstractTask;
import com.recap.core.CompositeTask;
import com.recap.core.Task;
import com.recap.replay.gui.overlay.GuiReplayOverlay;
import org.lwjgl.input.Keyboard;

public class OverlayGui {
    public static Task whileOpened(Task...tasks) {
        Task[] nTasks = new Task[tasks.length + 2];
        System.arraycopy(tasks, 0, nTasks, 1, tasks.length);
        nTasks[0] = new Open();
        nTasks[tasks.length + 1] = new Close();
        return new CompositeTask(nTasks);
    }

    public static class Open extends AbstractTask {
        @Override
        protected void init() {
            press(Keyboard.KEY_T);
            expectGui(GuiReplayOverlay.class, done -> future.set(null));
        }
    }

    public static class Close extends AbstractTask {
        @Override
        protected void init() {
            press(Keyboard.KEY_ESCAPE);
            expectGuiClosed(() -> future.set(null));
        }
    }
}
