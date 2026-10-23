package com.replaymod.core.gui;

import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.RenderInfo;
import de.johni0702.minecraft.gui.container.AbstractGuiContainer;
import de.johni0702.minecraft.gui.container.GuiPanel;
import de.johni0702.minecraft.gui.container.GuiScreen;
import de.johni0702.minecraft.gui.container.VanillaGuiScreen;
import de.johni0702.minecraft.gui.element.GuiElement;
import de.johni0702.minecraft.gui.layout.CustomLayout;
import de.johni0702.minecraft.gui.layout.VerticalLayout;
import de.johni0702.minecraft.gui.utils.EventRegistrations;
import de.johni0702.minecraft.gui.utils.lwjgl.Dimension;
import de.johni0702.minecraft.gui.utils.lwjgl.ReadableDimension;
import de.johni0702.minecraft.gui.versions.callbacks.InitScreenCallback;

import static com.replaymod.core.versions.MCVer.getMinecraft;

public class GuiBackgroundProcesses extends EventRegistrations {
    private GuiPanel panel = new GuiPanel().setLayout(new VerticalLayout().setSpacing(10));
    private boolean reentrant;

    { on(InitScreenCallback.EVENT, (screen, buttons) -> onGuiInit(screen)); }
    private void onGuiInit(net.minecraft.client.gui.screen.Screen guiScreen) {
        if (guiScreen != getMinecraft().currentScreen) return; // people tend to construct GuiScreens without opening them

        VanillaGuiScreen vanillaGui;
        // TODO Workaround for #473 and #501 where another mod opens a new gui in response to the MCGuiScreen.init we
        //      call from VanillaGuiScreen.register.
        //      Ideally, we don't have an hidden inner MCGuiScreen and instead have a common parent class for
        //      AbstractGuiScreen, AbstractGuiOverlay and VanillaGuiScreen which deals with the common things. That's
        //      quite a bit of changes though, so I'll keep that for 2.7 and have this workaround until then.
        if (reentrant) return;
        try {
            reentrant = true;
            vanillaGui = VanillaGuiScreen.wrap(guiScreen);
        } finally {
            reentrant = false;
        }
        vanillaGui.setLayout(new CustomLayout<GuiScreen>(vanillaGui.getLayout()) {
            @Override
            protected void layout(GuiScreen container, int width, int height) {
                pos(panel, width - 5 - width(panel), 5);
            }
        }).addElements(null, panel);
    }

    public void addProcess(GuiElement<?> element) {
        panel.addElements(new VerticalLayout.Data(1.0),
                new Element(element));
    }

    public void removeProcess(GuiElement<?> element) {
        for (GuiElement child : panel.getChildren()) {
            if (((Element) child).inner == element) {
                panel.removeElement(child);
                return;
            }
        }
    }

    private static class Element extends AbstractGuiContainer<Element> {
        private GuiElement inner;

        Element(GuiElement inner) {
            this.inner = inner;
            addElements(null, inner);

            setLayout(new CustomLayout<Element>() {
                @Override
                protected void layout(Element container, int width, int height) {
                    pos(inner, 10, 10);
                    size(inner, width - 20, height - 20);
                }
            });
        }

        @Override
        public int getLayer() {
            return 1;
        }

        @Override
        public ReadableDimension getMinSize() {
            ReadableDimension minSize = inner.getMinSize();
            return new Dimension(minSize.getWidth() + 20, minSize.getHeight() + 20);
        }

        @Override
        public void draw(GuiRenderer renderer, ReadableDimension size, RenderInfo renderInfo) {
            int w = size.getWidth();
            int h = size.getHeight();

            // Recap Mod: flat card instead of the original nine-slice tooltip texture.
            if (w > 6 && h > 6) {
                RecapTheme.roundedRect(renderer, 2, 3, w - 2, h - 2, 0x44000000);
                RecapTheme.roundedRect(renderer, 0, 0, w, h, RecapTheme.CARD);
                renderer.drawRect(2, 0, w - 4, 1, RecapTheme.BORDER);
                renderer.drawRect(2, h - 1, w - 4, 1, RecapTheme.BORDER);
                renderer.drawRect(0, 2, 1, h - 4, RecapTheme.BORDER);
                renderer.drawRect(w - 1, 2, 1, h - 4, RecapTheme.BORDER);
                renderer.drawRect(1, 2, 2, Math.min(12, h - 4), RecapTheme.ACCENT);
            }

            super.draw(renderer, size, renderInfo);
        }

        @Override
        protected Element getThis() {
            return this;
        }
    }
}
