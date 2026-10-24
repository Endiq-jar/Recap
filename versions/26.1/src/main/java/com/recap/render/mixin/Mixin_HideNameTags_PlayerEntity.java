package com.recap.render.mixin;

import com.recap.core.versions.MCVer;
import com.recap.render.hooks.EntityRendererHandler;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class Mixin_HideNameTags_PlayerEntity {
    // 1.21.11 and below
}
