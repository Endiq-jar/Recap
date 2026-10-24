package com.recap.render.processor;

import com.recap.render.rendering.Frame;

public class DummyProcessor<F extends Frame> extends AbstractFrameProcessor<F, F> {
    @Override
    public F process(F rawFrame) {
        return rawFrame;
    }
}
