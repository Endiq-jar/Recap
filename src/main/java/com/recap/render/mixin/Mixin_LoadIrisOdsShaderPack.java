//#if MC>=11600
package com.recap.render.mixin;

import com.recap.render.capturer.IrisODSFrameCapturer;
import net.coderbot.iris.Iris;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.file.Path;

@Pseudo
@Mixin(value = Iris.class, remap = false)
public class Mixin_LoadIrisOdsShaderPack {
    @Redirect(method = "loadExternalShaderpack", at = @At(value = "INVOKE", target = "Lnet/coderbot/iris/Iris;getShaderpacksDirectory()Ljava/nio/file/Path;"))
    private static Path loadRecapModOdsPack(String name) {
        if (IrisODSFrameCapturer.INSTANCE != null && IrisODSFrameCapturer.SHADER_PACK_NAME.equals(name)) {
            return FabricLoader.getInstance().getModContainer("recap")
                    .orElseThrow(() -> new RuntimeException("Failed to get mod container for RecapMod"))
                    .getRootPath();
        } else {
            return Iris.getShaderpacksDirectory();
        }
    }
}
//#endif
