package com.recap.extras.playeroverview;

import com.recap.core.KeyBindingRegistry;
import com.recap.core.RecapMod;
import com.recap.core.events.PreRenderHandCallback;
import com.recap.core.utils.Utils;
import com.recap.extras.Extra;
import com.recap.replay.ReplayHandler;
import com.recap.replay.RecapModReplay;
import com.recap.replay.camera.CameraEntity;
import com.recap.replay.events.ReplayClosedCallback;
import com.recap.replay.events.ReplayOpenedCallback;
import com.replaymod.replaystudio.lib.guava.base.Optional;
import de.johni0702.minecraft.gui.utils.EventRegistrations;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

//#if MC>=11400
import java.util.stream.Collectors;
//#else
//$$ import org.lwjgl.input.Keyboard;
//#if MC>=10800
//$$ import com.google.common.base.Predicate;
//#else
//$$ import cpw.mods.fml.common.eventhandler.EventPriority;
//$$ import cpw.mods.fml.common.eventhandler.SubscribeEvent;
//$$ import net.minecraftforge.client.event.RenderPlayerEvent;
//$$ import java.util.stream.Collectors;
//#endif
//#endif

import java.io.IOException;
import java.util.*;

import static com.recap.core.versions.MCVer.*;

public class PlayerOverview extends EventRegistrations implements Extra {
    private RecapModReplay module;

    private final Set<UUID> hiddenPlayers = new HashSet<>();
    private boolean savingEnabled;

    @Override
    public void register(final RecapMod mod) throws Exception {
        this.module = RecapModReplay.instance;

        mod.getKeyBindingRegistry().registerKeyBinding("recap.input.playeroverview", KeyBindingRegistry.KeyCodes.KEY_B, new Runnable() {
            @Override
            public void run() {
                if (module.getReplayHandler() != null) {
                    //#if MC>=11400
                    List<PlayerEntity> players = mod.getMinecraft().world.getPlayers()
                            .stream()
                            .map(it -> (PlayerEntity) it)
                            .filter(it -> !(it instanceof CameraEntity))
                            .collect(Collectors.toList());
                    //#else
                    //$$ @SuppressWarnings("unchecked")
                    //#if MC>=10800
                    //$$ List<EntityPlayer> players = mod.getMinecraft().world.getPlayers(EntityPlayer.class, new Predicate() {
                    //$$     @Override
                    //$$     public boolean apply(Object input) {
                    //$$         return !(input instanceof CameraEntity); // Exclude the camera entity
                    //$$     }
                    //$$ });
                    //#else
                    //$$ List<EntityPlayer> players = mod.getMinecraft().theWorld.playerEntities;
                    //$$ players = players.stream()
                    //$$         .filter(it -> !(it instanceof CameraEntity)) // Exclude the camera entity
                    //$$         .collect(Collectors.toList());
                    //#endif
                    //#endif
                    if (!Utils.isCtrlDown()) {
                        // Hide all players that have an UUID v2 (commonly used for NPCs)
                        Iterator<PlayerEntity> iter = players.iterator();
                        while (iter.hasNext()) {
                            UUID uuid = iter.next().getGameProfile().getId();
                            if (uuid != null && uuid.version() == 2) {
                                iter.remove();
                            }
                        }
                    }
                    new PlayerOverviewGui(PlayerOverview.this, players).display();
                }
            }
        }, true);

        register();
    }

    public boolean isHidden(UUID uuid) {
        return hiddenPlayers.contains(uuid);
    }

    public void setHidden(UUID uuid, boolean hidden) {
        if (hidden) {
            hiddenPlayers.add(uuid);
        } else {
            hiddenPlayers.remove(uuid);
        }
    }

    { on(ReplayOpenedCallback.EVENT, this::onReplayOpen); }
    private void onReplayOpen(ReplayHandler replayHandler) throws IOException {
        Optional<Set<UUID>> savedData = replayHandler.getReplayFile().getInvisiblePlayers();
        if (savedData.isPresent()) {
            hiddenPlayers.addAll(savedData.get());
            savingEnabled = true;
        } else {
            savingEnabled = false;
        }
    }

    { on(ReplayClosedCallback.EVENT, this::onReplayClose); }
    private void onReplayClose(ReplayHandler replayHandler) {
        hiddenPlayers.clear();
    }

    { on(PreRenderHandCallback.EVENT, this::shouldHideHand); }
    private boolean shouldHideHand() {
        Entity view = module.getCore().getMinecraft().getCameraEntity();
        return view != null && isHidden(view.getUuid());
    }

    // See MixinRender for why this is 1.7.10 only
    //#if MC<=10710
    //$$ @SubscribeEvent(priority = EventPriority.HIGHEST)
    //$$ public void preRenderPlayer(RenderPlayerEvent.Pre event) {
    //$$     if (isHidden(event.entityPlayer.getUniqueID())) {
    //$$         event.setCanceled(true);
    //$$     }
    //$$ }
    //#endif

    public boolean isSavingEnabled() {
        return savingEnabled;
    }

    public void setSavingEnabled(boolean savingEnabled) {
        this.savingEnabled = savingEnabled;
    }

    public void saveHiddenPlayers() {
        if (savingEnabled) {
            try {
                module.getReplayHandler().getReplayFile().writeInvisiblePlayers(hiddenPlayers);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
