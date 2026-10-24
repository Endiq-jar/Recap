package com.recap.extras.advancedscreenshots;

import com.recap.core.RecapMod;
import com.recap.core.utils.Utils;
import com.recap.core.versions.MCVer;
import com.recap.extras.RecapModExtras;
import com.recap.render.frame.BitmapFrame;
import com.recap.render.rendering.Channel;
import com.recap.render.rendering.FrameConsumer;
import com.recap.replay.RecapModReplay;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import de.johni0702.minecraft.gui.versions.Image;
import net.minecraft.util.crash.CrashReport;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ScreenshotWriter implements FrameConsumer<BitmapFrame> {

    private final File outputFile;

    public ScreenshotWriter(File outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public void consume(Map<Channel, BitmapFrame> channels) {
        BitmapFrame frame = channels.get(Channel.BRGA);

        // skip the first frame, in which not all chunks are properly loaded
        if (frame.getFrameId() == 0) return;

        final ReadableDimension frameSize = frame.getSize();
        try (Image img = new Image(frameSize.getWidth(), frameSize.getHeight())) {
            for (int y = 0; y < frameSize.getHeight(); y++) {
                for (int x = 0; x < frameSize.getWidth(); x++) {
                    byte b = frame.getByteBuffer().get();
                    byte g = frame.getByteBuffer().get();
                    byte r = frame.getByteBuffer().get();
                    byte a = frame.getByteBuffer().get();

                    img.setRGBA(x, y, r, g, b, 0xff);
                }
            }

            outputFile.getParentFile().mkdirs();
            img.writePNG(outputFile);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            CrashReport report = CrashReport.create(e, "Exporting frame");
            MCVer.getMinecraft().setCrashReport(report);
        } catch (Throwable t) {
            CrashReport report = CrashReport.create(t, "Exporting frame");

            RecapMod.instance.runLater(() -> Utils.error(RecapModExtras.LOGGER,
                    RecapModReplay.instance.getReplayHandler().getOverlay(),
                    report, null));
        }
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean isParallelCapable() {
        return false;
    }
}
