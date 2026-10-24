package com.recap.render;

import com.recap.core.Module;
import com.recap.core.RecapMod;
import com.recap.core.utils.Utils;
import com.recap.render.utils.RenderJob;
import com.recap.replay.ReplayHandler;
import com.recap.replay.events.ReplayClosedCallback;
import com.recap.replay.events.ReplayOpenedCallback;
import com.replaymod.replaystudio.replay.ReplayFile;
import de.johni0702.minecraft.gui.container.VanillaGuiScreen;
import de.johni0702.minecraft.gui.utils.EventRegistrations;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RecapModRender extends EventRegistrations implements Module {
    { instance = this; }
    public static RecapModRender instance;

    private RecapMod core;

    public static Logger LOGGER = LogManager.getLogger();

    private ReplayFile replayFile;
    private final List<RenderJob> renderQueue = new ArrayList<>();

    public RecapModRender(RecapMod core) {
        this.core = core;

        core.getSettingsRegistry().register(Setting.class);
    }

    public RecapMod getCore() {
        return core;
    }

    @Override
    public void initClient() {
        register();
    }

    public File getVideoFolder() {
        String path = core.getSettingsRegistry().get(Setting.RENDER_PATH);
        File folder = new File(path.startsWith("./") ? core.getMinecraft().runDirectory : null, path);
        try {
            FileUtils.forceMkdir(folder);
        } catch (IOException e) {
            throw new CrashException(CrashReport.create(e, "Cannot create video folder."));
        }
        return folder;
    }

    public Path getRenderSettingsPath() {
        return core.getMinecraft().runDirectory.toPath().resolve("config/recap-rendersettings.json");
    }

    public List<RenderJob> getRenderQueue() {
        return renderQueue;
    }

    { on(ReplayOpenedCallback.EVENT, this::onReplayOpened); }
    private void onReplayOpened(ReplayHandler replayHandler) {
        replayFile = replayHandler.getReplayFile();
        try {
            renderQueue.addAll(RenderJob.readQueue(replayFile));
        } catch (IOException e) {
            throw new CrashException(CrashReport.create(e, "Reading timeline"));
        }
    }

    { on(ReplayClosedCallback.EVENT, replayHandler -> onReplayClosed()); }
    private void onReplayClosed() {
        renderQueue.clear();
        replayFile = null;
    }

    public void saveRenderQueue() {
        try {
            RenderJob.writeQueue(replayFile, renderQueue);
        } catch (IOException e) {
            e.printStackTrace();
            VanillaGuiScreen screen = VanillaGuiScreen.wrap(getCore().getMinecraft().currentScreen);
            CrashReport report = CrashReport.create(e, "Reading timeline");
            Utils.error(LOGGER, screen, report, () -> {});
        }
    }
}
