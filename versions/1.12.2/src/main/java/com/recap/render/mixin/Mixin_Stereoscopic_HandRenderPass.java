package com.recap.render.mixin;

import com.recap.render.capturer.StereoscopicOpenGlFrameCapturer;
import com.recap.render.hooks.EntityRendererHandler;
import com.recap.replay.camera.CameraEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityRenderer.class)
public abstract class Mixin_Stereoscopic_HandRenderPass implements EntityRendererHandler.IEntityRenderer {
    @ModifyVariable(method = "renderHand", at = @At("HEAD"), argsOnly = true)
    private int recapModRender_renderSpectatorHand(int renderPass) {
        EntityRendererHandler handler = recapModRender_getHandler();
        if (handler != null) {
            Entity currentEntity = Minecraft.getMinecraft().getRenderViewEntity();
            if (currentEntity instanceof EntityPlayer && !(currentEntity instanceof CameraEntity)) {
                if (renderPass == 2) { // Need to update render pass
                    renderPass = handler.data == StereoscopicOpenGlFrameCapturer.Data.LEFT_EYE ? 1 : 0;
                }
            }
        }
        return renderPass;
    }
}
