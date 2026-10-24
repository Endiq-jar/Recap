package com.recap.extras.advancedscreenshots;

import com.recap.core.versions.MCVer;
import com.recap.render.RenderSettings;
import com.recap.render.blend.BlendState;
import com.recap.render.capturer.RenderInfo;
import com.recap.render.hooks.ForceChunkLoadingHook;
import com.recap.render.rendering.Pipelines;
import de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.util.crash.CrashReport;

//#if MC < 26.3
import static com.recap.core.versions.MCVer.resizeMainWindow;
//#endif

public class ScreenshotRenderer implements RenderInfo {

    private final MinecraftClient mc = MCVer.getMinecraft();

    private final RenderSettings settings;

    private int framesDone;

    public ScreenshotRenderer(RenderSettings settings) {
        this.settings = settings;
    }

    public boolean renderScreenshot() throws Throwable {
        try {
            //#if MC < 26.3
            Window window = mc.getWindow();
            int widthBefore = window.getFramebufferWidth();
            int heightBefore = window.getFramebufferHeight();
            //#endif

            ForceChunkLoadingHook clrg = new ForceChunkLoadingHook(mc.worldRenderer);

            if (settings.getRenderMethod() == RenderSettings.RenderMethod.BLEND) {
                BlendState.setState(new BlendState(settings.getOutputFile()));
                Pipelines.newBlendPipeline(this).run();
            } else {
                Pipelines.newPipeline(settings.getRenderMethod(), this,
                        new ScreenshotWriter(settings.getOutputFile())).run();
            }

            clrg.uninstall();

            //#if MC < 26.3
            resizeMainWindow(mc, widthBefore, heightBefore);
            //#endif
            return true;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            CrashReport report = CrashReport.create(e, "Creating Equirectangular Screenshot");
            MCVer.getMinecraft().setCrashReport(report);
        }
        return false;
    }

    @Override
    public ReadableDimension getFrameSize() {
        return new Dimension(settings.getVideoWidth(), settings.getVideoHeight());
    }

    @Override
    public int getFramesDone() {
        return framesDone;
    }

    @Override
    public int getTotalFrames() {
        // render 2 frames, because only the second contains all frames fully loaded
        return 2;
    }

    @Override
    public float updateForNextFrame() {
        framesDone++;
        return mc.getTickDelta();
    }

    @Override
    public RenderSettings getRenderSettings() {
        return settings;
    }
}
