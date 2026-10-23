package com.replaymod.recording.gui;

import com.replaymod.core.SettingsRegistry;
import com.replaymod.core.gui.RecapTheme;
import com.replaymod.recording.Setting;
import de.johni0702.minecraft.gui.GuiRenderer;
import de.johni0702.minecraft.gui.MinecraftGuiRenderer;
import de.johni0702.minecraft.gui.utils.EventRegistrations;
import de.johni0702.minecraft.gui.versions.callbacks.RenderHudCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.resource.language.I18n;

//#if MC>=12000
//$$ import net.minecraft.client.gui.DrawContext;
//#else
import net.minecraft.client.util.math.MatrixStack;
//#endif

/**
 * Renders the recording indicator in the top left corner of the screen.
 *
 * <p>Recap Mod uses a compact pill instead of the classic icon: a status dot, the state label and a
 * blinking dot while recording, all drawn with flat themed colours so it reads well on any background
 * without covering the hotbar area or the replay controls.</p>
 */
public class GuiRecordingOverlay extends EventRegistrations {
    private static final int PADDING_X = 8;
    private static final int DOT_SIZE = 6;
    private static final int HEIGHT = 16;

    private final MinecraftClient mc;
    private final SettingsRegistry settingsRegistry;
    private final GuiRecordingControls guiControls;

    public GuiRecordingOverlay(MinecraftClient mc, SettingsRegistry settingsRegistry, GuiRecordingControls guiControls) {
        this.mc = mc;
        this.settingsRegistry = settingsRegistry;
        this.guiControls = guiControls;
    }

    /** Render the recording indicator in the top left corner of the screen. */
    { on(RenderHudCallback.EVENT, (stack, partialTicks) -> renderRecordingIndicator(stack, partialTicks)); }
    //#if MC>=12000
    //$$ private void renderRecordingIndicator(DrawContext stack, float partialTicks) {
    //#else
    private void renderRecordingIndicator(MatrixStack stack, float partialTicks) {
    //#endif
        if (guiControls.isStopped()) return;
        if (!settingsRegistry.get(Setting.INDICATOR)) return;

        TextRenderer fontRenderer = mc.textRenderer;
        boolean paused = guiControls.isPaused();
        String text = I18n.translate(paused ? "replaymod.gui.paused" : "replaymod.gui.recording").toUpperCase();
        int accent = paused ? RecapTheme.PAUSED : RecapTheme.RECORDING;

        MinecraftGuiRenderer renderer = new MinecraftGuiRenderer(stack);
        int width = PADDING_X * 2 + DOT_SIZE + 6 + fontRenderer.getWidth(text);
        int x = 10;
        int y = 10;

        RecapTheme.hudBar(renderer, x, y, width, HEIGHT);
        // Accent rail so the state is recognisable at a glance
        renderer.drawRect(x, y + 3, 2, HEIGHT - 6, accent);

        int dotX = x + PADDING_X;
        int dotY = y + (HEIGHT - DOT_SIZE) / 2;
        if (paused) {
            renderer.drawRect(dotX, dotY, DOT_SIZE, DOT_SIZE, RecapTheme.PAUSED);
        } else {
            // Blinking dot: 1s period, 60% duty cycle
            long phase = (System.currentTimeMillis() % 1000L);
            boolean on = phase < 600L;
            renderer.drawRect(dotX, dotY, DOT_SIZE, DOT_SIZE,
                    on ? RecapTheme.RECORDING : RecapTheme.withAlpha(RecapTheme.RECORDING, 0.35f));
        }

        renderer.drawString(dotX + DOT_SIZE + 6, y + (HEIGHT - fontRenderer.fontHeight) / 2 + 1,
                RecapTheme.TEXT, text);
    }
}
