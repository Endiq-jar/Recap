package com.recap.render.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.recap.render.RenderSettings;
import com.recap.render.hooks.EntityRendererHandler;
import com.recap.replay.camera.CameraEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.recap.core.versions.MCVer.*;

//#if MC>=11400
import net.minecraft.client.Camera;
//#else
//$$ import net.minecraft.client.renderer.EntityRenderer;
//#endif

@Mixin(value = Camera.class)
public abstract class Mixin_StabilizeCamera {
    @WrapOperation(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"))
    private void recapModRender_stabilizeCamera(Camera instance, float yRot, float xRot, Operation<Void> original) {
        if (getHandler() != null) {
            RenderSettings settings = getHandler().getSettings();

            if (settings.isStabilizeYaw()) {
                yRot = 0f;
            }

            if (settings.isStabilizePitch()) {
                xRot = 0f;
            }

            // Roll handled in MixinCamera
        }

        original.call(instance, yRot, xRot);
    }

    @Unique
    private EntityRendererHandler getHandler() {
        return ((EntityRendererHandler.IEntityRenderer) getMinecraft().gameRenderer).recapModRender_getHandler();
    }
}
