package dev.serverloginprofiles.session;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;

import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.multiplayer.ServerData;

import dev.serverloginprofiles.ServerLoginProfiles;
import dev.serverloginprofiles.mixin.MinecraftBridge;
import dev.serverloginprofiles.profile.LoginProfile;
import dev.serverloginprofiles.profile.ServerDataProfileAccess;
import dev.serverloginprofiles.yggdrasil.ThirdPartySessionService;
import dev.serverloginprofiles.yggdrasil.YggdrasilHttp;

/** Identity installation for the 1.21–1.21.8 Minecraft client layout. */
public final class IdentityController
{
    private static Snapshot launcher;
    private static String appliedAddress;
    private static int appliedFingerprint;

    private IdentityController() {}

    public static synchronized void selectFor(ServerData server)
    {
        captureLauncher();
        String key = server.ip == null ? "" : server.ip.strip().toLowerCase(Locale.ROOT);
        LoginProfile profile = ((ServerDataProfileAccess) server).slp$getLoginProfile();
        int fingerprint = profile.fingerprint();
        if (key.equals(appliedAddress) && fingerprint == appliedFingerprint) return;

        switch (profile.kind) {
            case OFFICIAL -> restoreLauncher();
            case OFFLINE -> applyOffline(profile);
            case YGGDRASIL -> applyYggdrasil(profile);
        }
        appliedAddress = key;
        appliedFingerprint = fingerprint;
        ServerLoginProfiles.LOG.info("Selected {} identity for {}", profile.kind, key);
    }

    /** Restores the launcher identity after leaving a multiplayer world. */
    public static synchronized void restoreAfterDisconnect()
    {
        if (launcher == null || appliedAddress == null) return;
        restoreLauncher();
        appliedAddress = null;
        appliedFingerprint = 0;
        ServerLoginProfiles.LOG.info("Restored launcher identity after disconnect");
    }

    private static void captureLauncher()
    {
        if (launcher != null) return;
        Minecraft game = Minecraft.getInstance();
        MinecraftBridge bridge = (MinecraftBridge) game;
        launcher = new Snapshot(game.getUser(), bridge.getMinecraftSessionService(),
            bridge.getProfileFuture(), bridge.getUserApiService(), bridge.getProfileKeyPairManager());
    }

    private static void restoreLauncher()
    {
        install(launcher.user, launcher.sessionService, launcher.profileFuture,
            launcher.userApi, launcher.keyManager);
    }

    private static void applyOffline(LoginProfile profile)
    {
        String name = profile.offlineName == null ? "" : profile.offlineName.strip();
        if (name.isEmpty()) throw new LoginProfileException("serverloginprofiles.error.offline_name");
        UUID id = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        User user = new User(name, id, "offline", Optional.empty(), Optional.empty(), User.Type.LEGACY);
        install(user, launcher.sessionService, CompletableFuture.completedFuture(null),
            UserApiService.OFFLINE, ProfileKeyPairManager.EMPTY_KEY_MANAGER);
    }

    private static void applyYggdrasil(LoginProfile profile)
    {
        if (blank(profile.accessToken) || blank(profile.playerId) || blank(profile.playerName)
            || blank(profile.yggdrasilRoot)) {
            throw new LoginProfileException("serverloginprofiles.error.not_authenticated");
        }
        UUID id = parseUuid(profile.playerId);
        User user = new User(profile.playerName, id, profile.accessToken, Optional.empty(),
            Optional.ofNullable(profile.clientToken).filter(token -> !token.isBlank()), User.Type.LEGACY);
        MinecraftSessionService session = new ThirdPartySessionService(profile.yggdrasilRoot);
        CompletableFuture<ProfileResult> future = CompletableFuture.supplyAsync(
            () -> session.fetchProfile(id, false), YggdrasilHttp.executor());
        install(user, session, future, UserApiService.OFFLINE,
            ProfileKeyPairManager.EMPTY_KEY_MANAGER);
    }

    private static void install(User user, MinecraftSessionService service,
        CompletableFuture<ProfileResult> future, UserApiService userApi,
        ProfileKeyPairManager keyManager)
    {
        MinecraftBridge bridge = (MinecraftBridge) Minecraft.getInstance();
        bridge.setUser(user);
        bridge.setMinecraftSessionService(service);
        bridge.setProfileFuture(future);
        bridge.setUserApiService(userApi);
        bridge.setProfileKeyPairManager(keyManager);
        ActiveSessionService.select(service);
    }

    private static boolean blank(String value)
    {
        return value == null || value.isBlank();
    }

    private static UUID parseUuid(String raw)
    {
        String value = raw.replace("-", "");
        if (value.length() != 32) throw new LoginProfileException("serverloginprofiles.error.profile_id");
        return UUID.fromString(value.substring(0, 8) + "-" + value.substring(8, 12) + "-"
            + value.substring(12, 16) + "-" + value.substring(16, 20) + "-" + value.substring(20));
    }

    private record Snapshot(User user, MinecraftSessionService sessionService,
        CompletableFuture<ProfileResult> profileFuture, UserApiService userApi,
        ProfileKeyPairManager keyManager) {}
}
