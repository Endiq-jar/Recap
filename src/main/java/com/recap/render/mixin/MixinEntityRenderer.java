package com.recap.render.mixin;

import com.recap.render.hooks.EntityRendererHandler;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameRenderer.class)
public abstract class MixinEntityRenderer implements EntityRendererHandler.IEntityRenderer {
    private EntityRendererHandler recapModRender_handler;

    @Override
    public void recapModRender_setHandler(EntityRendererHandler handler) {
        this.recapModRender_handler = handler;
    }

    @Override
    public EntityRendererHandler recapModRender_getHandler() {
        return recapModRender_handler;
    }
}
