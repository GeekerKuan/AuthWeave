package dev.serverloginprofiles.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;

import dev.serverloginprofiles.session.IdentityController;

/** Keeps a per-server identity from leaking into menus or integrated worlds. */
@Mixin(Minecraft.class)
public abstract class MinecraftLifecycleMixin
{
    private MinecraftLifecycleMixin() {}

    @Inject(method = "clearClientLevel", at = @At("TAIL"))
    private void restoreLauncherIdentity(CallbackInfo callback)
    {
        IdentityController.restoreAfterDisconnect();
    }

    @Inject(method = "doWorldLoad", at = @At("HEAD"))
    private void restoreBeforeIntegratedWorld(CallbackInfo callback)
    {
        IdentityController.restoreAfterDisconnect();
    }
}
