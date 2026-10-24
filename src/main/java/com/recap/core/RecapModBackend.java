package com.recap.core;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;

import static com.recap.core.RecapMod.MOD_ID;

public class RecapModBackend implements ClientModInitializer {
    private final RecapMod mod = new RecapMod(this);

    @Override
    public void onInitializeClient() {
        mod.initModules();
    }

    public String getVersion() {
        return FabricLoader.getInstance().getModContainer(MOD_ID)
                .orElseThrow(IllegalStateException::new)
                .getMetadata().getVersion().toString();
    }

    public String getMinecraftVersion() {
        //#if MC>=12106
        //$$ return SharedConstants.getGameVersion().comp_4025();
        //#else
        return SharedConstants.getGameVersion().getName();
        //#endif
    }

    public boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id.toLowerCase());
    }
}
