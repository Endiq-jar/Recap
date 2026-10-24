package com.recap.render.mixin;

//#if MC>=10800
import com.recap.compat.shaders.ShaderReflection;
import com.recap.render.hooks.ForceChunkLoadingHook;
import com.recap.render.hooks.IForceChunkLoading;
import net.minecraft.client.render.VisibleRegion;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC>=11400
import net.minecraft.client.render.Camera;
//#else
//$$ import net.minecraft.entity.Entity;
//#endif

//#if MC<10904
//$$ import net.minecraft.client.renderer.chunk.RenderChunk;
//$$ import net.minecraft.util.BlockPos;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//#endif

@Mixin(WorldRenderer.class)
public abstract class Mixin_ForceChunkLoading implements IForceChunkLoading {
    private ForceChunkLoadingHook recapModRender_hook;

    @Override
    public void recapModRender_setHook(ForceChunkLoadingHook hook) {
        this.recapModRender_hook = hook;
    }

    private boolean recapModRender_passThroughSetupTerrain;

    @Shadow
    private boolean needsTerrainUpdate;

    @Shadow
    private ChunkBuilder chunkBuilder;

    @Shadow
    public abstract void setUpTerrain(
            //#if MC>=11400
            Camera viewEntity,
            //#else
            //$$ Entity viewEntity,
            //$$ double partialTicks,
            //#endif
            VisibleRegion camera,
            int frameCount,
            boolean playerSpectator
    );

    @Inject(method = "setUpTerrain", at = @At("HEAD"), cancellable = true)
    private void recapModRender_setupTerrain(
            //#if MC>=11400
            Camera viewEntity,
            //#else
            //$$ Entity viewEntity,
            //$$ double partialTicks,
            //#endif
            VisibleRegion camera,
            int frameCount,
            boolean playerSpectator,
            CallbackInfo ci
    ) throws IllegalAccessException {
        if (ShaderReflection.shaders_isShadowPass != null && (boolean) ShaderReflection.shaders_isShadowPass.get(null)) {
            return;
        }
        if (recapModRender_hook != null && !recapModRender_passThroughSetupTerrain) {
            recapModRender_passThroughSetupTerrain = true;

            do {
                setUpTerrain(
                        viewEntity,
                        //#if MC<11400
                        //$$ partialTicks,
                        //#endif
                        camera,
                        recapModRender_hook.nextFrameId(),
                        playerSpectator
                );
                recapModRender_hook.updateChunks();
            } while (this.needsTerrainUpdate);

            this.needsTerrainUpdate = true;

            recapModRender_passThroughSetupTerrain = false;
            ci.cancel();
        }
    }

    //#if MC<10904
    //$$ @Inject(method = "isPositionInRenderChunk", at = @At("HEAD"), cancellable = true)
    //$$ public void recapModRender_isPositionInRenderChunk(BlockPos pos, RenderChunk chunk, CallbackInfoReturnable<Boolean> ci) {
    //$$     if (recapModRender_hook != null) {
    //$$         ci.setReturnValue(true);
    //$$     }
    //$$ }
    //#endif

    @Inject(method = "updateChunks", at = @At("HEAD"), cancellable = true)
    public void recapModRender_updateChunks(long finishTimeNano, CallbackInfo ci) {
        if (recapModRender_hook != null) {
            recapModRender_hook.updateChunks();
            ci.cancel();
        }
    }

    // Prior to 1.9.4, MC always uses the same ChunkRenderDispatcher instance
    //#if MC>=10904
    @Inject(method = "setWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/ChunkBuilder;stop()V"))
    private void stopWorkerThreadsAndChunkLoadingRenderGlobal(CallbackInfo ci) {
        if (recapModRender_hook != null) {
            recapModRender_hook.updateRenderDispatcher(null);
        }
    }
    //#endif

    @Inject(method = "reload", at = @At(value = "RETURN"))
    private void setupChunkLoadingRenderGlobal(CallbackInfo ci) {
        if (recapModRender_hook != null) {
            recapModRender_hook.updateRenderDispatcher(this.chunkBuilder);
        }
    }
}
//#endif
