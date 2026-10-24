package com.recap.render.processor;

import com.recap.render.rendering.Frame;
import com.recap.render.rendering.FrameProcessor;

import java.io.IOException;

public abstract class AbstractFrameProcessor<R extends Frame, P extends Frame> implements FrameProcessor<R, P> {
    @Override
    public void close() throws IOException {
    }
}
