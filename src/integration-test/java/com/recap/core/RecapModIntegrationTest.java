package com.recap.core;

import com.recap.core.regression.RegressionTest60;
import com.recap.core.regression.RegressionTest61;
import com.recap.core.regression.RegressionTest62;
import com.recap.extra.DownloadOpenEye;
import com.recap.online.SkipLogin;
import com.recap.recording.CreateSPWorld;
import com.recap.recording.ExitSPWorld;
import com.recap.replay.ExitReplay;
import com.recap.replay.LoadReplay;
import com.recap.replay.OpenReplayViewer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

import static com.recap.core.AbstractTask.mc;
import static com.recap.core.RecapModIntegrationTest.MOD_ID;
import static com.recap.core.utils.Utils.addCallback;

/**
 * Helper mod that initiates the integration tests.
 */
@Mod(modid = MOD_ID)
public class RecapModIntegrationTest {
    public static final String MOD_ID = "recap-integration-test";

    public static Logger LOGGER;

    @Mod.EventHandler
    public void init(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();

        // Make sure the game window doesn't have to remain in focus during the test
        mc.gameSettings.pauseOnLostFocus = false;

        // Vanilla uses our keys
        mc.gameSettings.keyBindAdvancements.setKeyCode(0);
        mc.gameSettings.keyBindLoadToolbar.setKeyCode(0);
        mc.gameSettings.keyBindSaveToolbar.setKeyCode(0);

        runTasks(
                new SkipLogin(),
                new DownloadOpenEye(),
                new CreateSPWorld(),
                new Wait(5000),
                new ExitSPWorld(),
                new OpenReplayViewer(),
                new LoadReplay(),

                new RegressionTest60(),
                new RegressionTest61(),
                new RegressionTest62(),

                // new AbstractTask() {}, // Uncomment to not exit on success (useful for writing more tests)
                new ExitReplay()
        );
    }

    private void runTasks(Task... tests) {
        addCallback(new CompositeTask(tests).execute(), success -> {
            if (!Minecraft.getMinecraft().hasCrashed) {
                LOGGER.info("===================================================");
                LOGGER.info("=                 ALL TESTS PASSED                =");
                LOGGER.info("===================================================");
                FMLCommonHandler.instance().exitJava(0, false);
            }
        }, error -> {
            LOGGER.error("Failed task:", error);
            LOGGER.error("===================================================");
            LOGGER.error("=                  TEST FAILED                    =");
            LOGGER.error("===================================================");
            FMLCommonHandler.instance().exitJava(1, false);
        });
    }
}
