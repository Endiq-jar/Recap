package com.recap.render.rendering;

import com.recap.render.RenderSettings;
import com.recap.render.blend.BlendFrameCapturer;
import com.recap.render.capturer.CubicOpenGlFrameCapturer;
import com.recap.render.capturer.CubicPboOpenGlFrameCapturer;
import com.recap.render.capturer.ODSFrameCapturer;
import com.recap.render.capturer.RenderInfo;
import com.recap.render.capturer.SimpleOpenGlFrameCapturer;
import com.recap.render.capturer.SimplePboOpenGlFrameCapturer;
import com.recap.render.capturer.StereoscopicOpenGlFrameCapturer;
import com.recap.render.capturer.StereoscopicPboOpenGlFrameCapturer;
import com.recap.render.capturer.WorldRenderer;
import com.recap.render.frame.CubicOpenGlFrame;
import com.recap.render.frame.ODSOpenGlFrame;
import com.recap.render.frame.OpenGlFrame;
import com.recap.render.frame.BitmapFrame;
import com.recap.render.frame.StereoscopicOpenGlFrame;
import com.recap.render.hooks.EntityRendererHandler;
import com.recap.render.processor.CubicToBitmapProcessor;
import com.recap.render.processor.DummyProcessor;
import com.recap.render.processor.EquirectangularToBitmapProcessor;
import com.recap.render.processor.ODSToBitmapProcessor;
import com.recap.render.processor.OpenGlToBitmapProcessor;
import com.recap.render.processor.StereoscopicToBitmapProcessor;
import com.recap.render.utils.PixelBufferObject;

import java.util.Map;

public class Pipelines {
    public static Pipeline newPipeline(RenderSettings.RenderMethod method, RenderInfo renderInfo, FrameConsumer<BitmapFrame> consumer) {
        switch (method) {
            case DEFAULT:
                return newDefaultPipeline(renderInfo, consumer);
            case STEREOSCOPIC:
                return newStereoscopicPipeline(renderInfo, consumer);
            case CUBIC:
                return newCubicPipeline(renderInfo, consumer);
            case EQUIRECTANGULAR:
                return newEquirectangularPipeline(renderInfo, consumer);
            case ODS:
                return newODSPipeline(renderInfo, consumer);
            case BLEND:
                throw new UnsupportedOperationException("Use newBlendPipeline instead!");
        }
        throw new UnsupportedOperationException("Unknown method: " + method);
    }

    public static Pipeline<OpenGlFrame, BitmapFrame> newDefaultPipeline(RenderInfo renderInfo, FrameConsumer<BitmapFrame> consumer) {
        RenderSettings settings = renderInfo.getRenderSettings();
        WorldRenderer worldRenderer = new EntityRendererHandler(settings, renderInfo);
        FrameCapturer<OpenGlFrame> capturer;
        if (PixelBufferObject.SUPPORTED || settings.isDepthMap()) {
            capturer = new SimplePboOpenGlFrameCapturer(worldRenderer, renderInfo);
        } else {
            capturer = new SimpleOpenGlFrameCapturer(worldRenderer, renderInfo);
        }
        return new Pipeline<>(worldRenderer, capturer, new OpenGlToBitmapProcessor(), consumer);
    }

    public static Pipeline<StereoscopicOpenGlFrame, BitmapFrame> newStereoscopicPipeline(RenderInfo renderInfo, FrameConsumer<BitmapFrame> consumer) {
        RenderSettings settings = renderInfo.getRenderSettings();
        WorldRenderer worldRenderer = new EntityRendererHandler(settings, renderInfo);
        FrameCapturer<StereoscopicOpenGlFrame> capturer;
        if (PixelBufferObject.SUPPORTED || settings.isDepthMap()) {
            capturer = new StereoscopicPboOpenGlFrameCapturer(worldRenderer, renderInfo);
        } else {
            capturer = new StereoscopicOpenGlFrameCapturer(worldRenderer, renderInfo);
        }
        return new Pipeline<>(worldRenderer, capturer, new StereoscopicToBitmapProcessor(), consumer);
    }

    public static Pipeline<CubicOpenGlFrame, BitmapFrame> newCubicPipeline(RenderInfo renderInfo, FrameConsumer<BitmapFrame> consumer) {
        RenderSettings settings = renderInfo.getRenderSettings();
        WorldRenderer worldRenderer = new EntityRendererHandler(settings, renderInfo);
        FrameCapturer<CubicOpenGlFrame> capturer;
        if (PixelBufferObject.SUPPORTED || settings.isDepthMap()) {
            capturer = new CubicPboOpenGlFrameCapturer(worldRenderer, renderInfo, settings.getVideoWidth() / 4);
        } else {
            capturer = new CubicOpenGlFrameCapturer(worldRenderer, renderInfo, settings.getVideoWidth() / 4);
        }
        return new Pipeline<>(worldRenderer, capturer, new CubicToBitmapProcessor(), consumer);
    }

    public static Pipeline<CubicOpenGlFrame, BitmapFrame> newEquirectangularPipeline(RenderInfo renderInfo, FrameConsumer<BitmapFrame> consumer) {
        RenderSettings settings = renderInfo.getRenderSettings();
        WorldRenderer worldRenderer = new EntityRendererHandler(settings, renderInfo);

        EquirectangularToBitmapProcessor processor = new EquirectangularToBitmapProcessor(settings.getVideoWidth(),
                settings.getVideoHeight(), settings.getSphericalFovX());

        FrameCapturer<CubicOpenGlFrame> capturer;
        if (PixelBufferObject.SUPPORTED || settings.isDepthMap()) {
            capturer = new CubicPboOpenGlFrameCapturer(worldRenderer, renderInfo, processor.getFrameSize());
        } else {
            capturer = new CubicOpenGlFrameCapturer(worldRenderer, renderInfo, processor.getFrameSize());
        }
        return new Pipeline<>(worldRenderer, capturer, processor, consumer);
    }

    public static Pipeline<ODSOpenGlFrame, BitmapFrame> newODSPipeline(RenderInfo renderInfo, FrameConsumer<BitmapFrame> consumer) {
        RenderSettings settings = renderInfo.getRenderSettings();
        WorldRenderer worldRenderer = new EntityRendererHandler(settings, renderInfo);

        ODSToBitmapProcessor processor = new ODSToBitmapProcessor(settings.getVideoWidth(),
                settings.getVideoHeight(), settings.getSphericalFovX());

        //#if MC>=11600
        boolean iris = net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("iris");
        FrameCapturer<ODSOpenGlFrame> capturer = iris
                ? new com.recap.render.capturer.IrisODSFrameCapturer(worldRenderer, renderInfo, processor.getFrameSize())
                : new ODSFrameCapturer(worldRenderer, renderInfo, processor.getFrameSize());
        //#else
        //$$ FrameCapturer<ODSOpenGlFrame> capturer = new ODSFrameCapturer(worldRenderer, renderInfo, processor.getFrameSize());
        //#endif
        return new Pipeline<>(worldRenderer, capturer, processor, consumer);
    }

    public static Pipeline<BitmapFrame, BitmapFrame> newBlendPipeline(RenderInfo renderInfo) {
        RenderSettings settings = renderInfo.getRenderSettings();
        WorldRenderer worldRenderer = new EntityRendererHandler(settings, renderInfo);
        FrameCapturer<BitmapFrame> capturer = new BlendFrameCapturer(worldRenderer, renderInfo);
        FrameConsumer<BitmapFrame> consumer = new FrameConsumer<BitmapFrame>() {
            @Override
            public void consume(Map<Channel, BitmapFrame> channels) {
            }

            @Override
            public void close() {
            }

            @Override
            public boolean isParallelCapable() {
                return true;
            }
        };
        return new Pipeline<>(worldRenderer, capturer, new DummyProcessor<>(), consumer);
    }
}
