package com.recap.recording.mixin;

import com.recap.core.versions.MCVer;
import com.recap.recording.RecapModRecording;
import com.recap.recording.packet.PacketListener;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLoginNetworkHandler.class)
public abstract class MixinNetHandlerLoginClient {

    @Final @Shadow
    private ClientConnection connection;

    @Inject(method = "onQueryRequest", at=@At("HEAD"))
    private void earlyInitiateRecording(LoginQueryRequestS2CPacket packet, CallbackInfo ci) {
        initiateRecording(packet);
    }

    @Inject(method = "onLoginSuccess", at=@At("HEAD"))
    private void lateInitiateRecording(LoginSuccessS2CPacket packet, CallbackInfo ci) {
        initiateRecording(packet);
    }

    private void initiateRecording(Packet<?> packet) {
        if (RecapModRecording.instance.getConnectionEventHandler().getPacketListener() != null) {
            return; // already recording
        }
        RecapModRecording.instance.initiateRecording(this.connection);
        PacketListener packetListener = RecapModRecording.instance.getConnectionEventHandler().getPacketListener();
        if (packetListener != null) {
            packetListener.save(packet);
        }
    }
}
