package dev.serverloginprofiles.mixin;

import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;

import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.server.Services;

/** Narrow field bridge required to atomically replace the active identity. */
@Mixin(Minecraft.class)
public interface MinecraftBridge
{
    /** Returns the current session bundle. */
    @Accessor
    Services getServices();

    /** Replaces the current session bundle. */
    @Accessor
    @Mutable
    void setServices(Services value);

    /** Replaces the current user. */
    @Accessor
    @Mutable
    void setUser(User value);

    /** Returns the current profile lookup future. */
    @Accessor
    CompletableFuture<ProfileResult> getProfileFuture();

    /** Replaces the current profile lookup future. */
    @Accessor
    @Mutable
    void setProfileFuture(CompletableFuture<ProfileResult> value);

    /** Returns the current user API client. */
    @Accessor
    UserApiService getUserApiService();

    /** Replaces the current user API client. */
    @Accessor
    @Mutable
    void setUserApiService(UserApiService value);

    /** Returns the current secure-chat key manager. */
    @Accessor
    ProfileKeyPairManager getProfileKeyPairManager();

    /** Replaces the secure-chat key manager. */
    @Accessor
    @Mutable
    void setProfileKeyPairManager(ProfileKeyPairManager value);
}
