package com.recap.core.regression;

import com.recap.core.AbstractTask;
import com.recap.core.CompositeTask;
import com.recap.core.Task;
import com.recap.replay.overlay.OverlayGui;
import com.recap.simplepathing.GuiPathingTasks;
import com.recap.simplepathing.gui.GuiEditKeyframe;

/**
 * Regression test: #61 NPE when saving in edit keyframe gui of last keyframe
 */
public class RegressionTest61 extends CompositeTask {
    public RegressionTest61() {
        super(new Task[]{
                OverlayGui.whileOpened(
                        // Place keyframe
                        Task.click(130, 50),
                        new GuiPathingTasks.ClickPositionKeyframeButton(),
                        AbstractTask.create(task -> {
                            // Double click keyframe
                            task.click(130, 50);
                            task.click(130, 50);
                            task.expectGui(GuiEditKeyframe.Position.class, gui -> {
                                task.click(gui.saveButton);
                                task.expectPopupClosed(() -> task.future.set(null));
                            });
                        }),
                        // Place second spectator keyframe
                        new GuiPathingTasks.ClickPositionKeyframeButton()
                ),
                new GuiPathingTasks.ClearKeyframeTimeline(),
        });
    }
}
