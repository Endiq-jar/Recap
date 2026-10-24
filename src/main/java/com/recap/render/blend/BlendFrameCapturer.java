package com.recap.render.blend;

import com.recap.render.capturer.RenderInfo;
import com.recap.render.capturer.WorldRenderer;
import com.recap.render.frame.BitmapFrame;
import com.recap.render.rendering.Channel;
import com.recap.render.rendering.FrameCapturer;
import com.recap.render.utils.ByteBufferPool;
import de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class BlendFrameCapturer implements FrameCapturer<BitmapFrame> {
    protected final WorldRenderer worldRenderer;
    protected final RenderInfo renderInfo;
    protected int framesDone;

    public BlendFrameCapturer(WorldRenderer worldRenderer, RenderInfo renderInfo) {
        this.worldRenderer = worldRenderer;
        this.renderInfo = renderInfo;
    }

    @Override
    public boolean isDone() {
        return framesDone >= renderInfo.getTotalFrames();
    }

    @Override
    public Map<Channel, BitmapFrame> process() {
        if (framesDone == 0) {
            BlendState.getState().setup();
        }

        renderInfo.updateForNextFrame();

        BlendState.getState().preFrame(framesDone);
        worldRenderer.renderWorld(MinecraftClient.getInstance().getTickDelta(), 0, 0, null);
        BlendState.getState().postFrame(framesDone);

        BitmapFrame frame = new BitmapFrame(framesDone++, new Dimension(0, 0), 0, ByteBufferPool.allocate(0));
        return Collections.singletonMap(Channel.BRGA, frame);
    }

    @Override
    public void close() throws IOException {
        BlendState.getState().tearDown();
        BlendState.setState(null);
    }
}
