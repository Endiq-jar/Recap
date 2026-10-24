package com.recap.replay.mixin;

import com.recap.replay.camera.CameraEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC>=10800
//#else
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//#endif

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinPlayerControllerMP {

    @Shadow
    private MinecraftClient client;

    //#if MC>=10800
    //#if MC>=11400
    @Inject(method = "isFlyingLocked", at=@At("HEAD"), cancellable = true)
    //#else
    //$$ @Inject(method = "isSpectator", at=@At("HEAD"), cancellable = true)
    //#endif
    private void recapModReplay_isSpectator(CallbackInfoReturnable<Boolean> ci) {
        if (this.client.player instanceof CameraEntity) { // this check should in theory not be required
            ci.setReturnValue(this.client.player.isSpectator());
        }
    }
    //#endif

    //#if MC<=10710
    //$$ @Shadow
    //$$ private NetHandlerPlayClient netClientHandler;
    //$$
    //$$ // Prevent the disconnect GUI from being opened during the short time when the replay is restarted
    //$$ // at which the old network manager is closed but still getting ticked (hence the disconnect GUI opening).
    //$$ @Inject(method = "updateController", at = @At("HEAD"), cancellable = true)
    //$$ private void recapModReplay_onlyTickNeverDisconnect(CallbackInfo ci) {
    //$$     if (RecapModReplay.instance.getReplayHandler() != null) {
    //$$         if (netClientHandler.getNetworkManager().isChannelOpen()) {
    //$$             netClientHandler.getNetworkManager().processReceivedPackets();
    //$$         }
    //$$         ci.cancel();
    //$$     }
    //$$ }
    //$$
    //$$ @Inject(method = "resetBlockRemoving", at = @At("HEAD"), cancellable = true)
    //$$ private void recapModReplay_skipWorldTick(CallbackInfo ci) {
    //$$     if (this.mc.theWorld == null) {
    //$$         ci.cancel();
    //$$     }
    //$$ }
    //#endif
}
