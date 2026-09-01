package dev.serverloginprofiles.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;

import dev.serverloginprofiles.session.ActiveSessionService;

/** Makes the legacy asynchronous skin cache honor the currently selected provider. */
@Mixin(targets = "net.minecraft.client.resources.SkinManager$1")
public abstract class SkinCacheServiceMixin
{
    @Redirect(
        method = "method_54647",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;unpackTextures("
                + "Lcom/mojang/authlib/properties/Property;)"
                + "Lcom/mojang/authlib/minecraft/MinecraftProfileTextures;"
        )
    )
    private static MinecraftProfileTextures unpackWithCurrentService(
        MinecraftSessionService capturedAtStartup,
        Property property
    )
    {
        return ActiveSessionService.currentOr(capturedAtStartup).unpackTextures(property);
    }
}
