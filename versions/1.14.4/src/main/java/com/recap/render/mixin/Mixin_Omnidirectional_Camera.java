package com.recap.render.mixin;

import com.recap.render.hooks.EntityRendererHandler;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public abstract class Mixin_Omnidirectional_Camera implements EntityRendererHandler.IEntityRenderer {
    @Redirect(method = "applyCameraTransformations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/Matrix4f;method_4929(DFFF)Lnet/minecraft/client/util/math/Matrix4f;"))
    private Matrix4f recapModRender_perspective$0(double fovY, float aspect, float zNear, float zFar) {
        return recapModRender_perspective((float) fovY, aspect, zNear, zFar);
    }

    @Redirect(method = "renderCenter", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/Matrix4f;method_4929(DFFF)Lnet/minecraft/client/util/math/Matrix4f;"))
    private Matrix4f recapModRender_perspective$1(double fovY, float aspect, float zNear, float zFar) {
        return recapModRender_perspective((float) fovY, aspect, zNear, zFar);
    }

    @Redirect(method = "renderAboveClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/Matrix4f;method_4929(DFFF)Lnet/minecraft/client/util/math/Matrix4f;"))
    private Matrix4f recapModRender_perspective$2(double fovY, float aspect, float zNear, float zFar) {
        return recapModRender_perspective((float) fovY, aspect, zNear, zFar);
    }

    private Matrix4f recapModRender_perspective(float fovY, float aspect, float zNear, float zFar) {
        if (recapModRender_getHandler() != null && recapModRender_getHandler().omnidirectional) {
            fovY = 90;
            aspect = 1;
        }
        return Matrix4f.method_4929(fovY, aspect, zNear, zFar);
    }
}
