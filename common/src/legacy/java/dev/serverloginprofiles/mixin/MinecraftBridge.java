package dev.serverloginprofiles.mixin;

import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;

import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;

/** Narrow 1.21–1.21.8 field bridge for atomic identity replacement. */
@Mixin(Minecraft.class)
public interface MinecraftBridge
{
    @Accessor
    @Mutable
    void setUser(User value);

    @Accessor
    MinecraftSessionService getMinecraftSessionService();

    @Accessor
    @Mutable
    void setMinecraftSessionService(MinecraftSessionService value);

    @Accessor
    CompletableFuture<ProfileResult> getProfileFuture();

    @Accessor
    @Mutable
    void setProfileFuture(CompletableFuture<ProfileResult> value);

    @Accessor
    UserApiService getUserApiService();

    @Accessor
    @Mutable
    void setUserApiService(UserApiService value);

    @Accessor
    ProfileKeyPairManager getProfileKeyPairManager();

    @Accessor
    @Mutable
    void setProfileKeyPairManager(ProfileKeyPairManager value);
}
