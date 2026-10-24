package com.recap.render.mixin;

import com.recap.render.hooks.EntityRendererHandler;
import net.minecraft.client.renderer.EntityRenderer;
import org.lwjgl.util.glu.Project;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public abstract class Mixin_Omnidirectional_Camera implements EntityRendererHandler.IEntityRenderer {
    @Redirect(method = "setupCameraTransform", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V", remap = false))
    private void recapModRender_gluPerspective$0(float fovY, float aspect, float zNear, float zFar) {
        recapModRender_gluPerspective(fovY, aspect, zNear, zFar);
    }

    //#if MC>=10800
    @Redirect(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V", remap = false))
    //#else
    //$$ @Redirect(method = "renderHand", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V", remap = false))
    //#endif
    private void recapModRender_gluPerspective$1(float fovY, float aspect, float zNear, float zFar) {
        recapModRender_gluPerspective(fovY, aspect, zNear, zFar);
    }

    //#if MC>=10800
    @Redirect(method = "renderCloudsCheck", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V", remap = false))
    private void recapModRender_gluPerspective$2(float fovY, float aspect, float zNear, float zFar) {
        recapModRender_gluPerspective(fovY, aspect, zNear, zFar);
    }
    //#endif

    private void recapModRender_gluPerspective(float fovY, float aspect, float zNear, float zFar) {
        if (recapModRender_getHandler() != null && recapModRender_getHandler().omnidirectional) {
            fovY = 90;
            aspect = 1;
        }
        Project.gluPerspective(fovY, aspect, zNear, zFar);
    }
}
