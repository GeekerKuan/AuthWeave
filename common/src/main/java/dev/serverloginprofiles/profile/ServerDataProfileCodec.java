package dev.serverloginprofiles.profile;

import java.util.Locale;

import net.minecraft.nbt.CompoundTag;

/** Versioned NBT representation stored inside each vanilla servers.dat entry. */
public final class ServerDataProfileCodec
{
    public static final String ROOT_KEY = "serverloginprofiles";
    private static final int FORMAT_VERSION = 1;

    private ServerDataProfileCodec() {}

    public static LoginProfile read(CompoundTag serverTag)
    {
        LoginProfile profile = new LoginProfile();
        if (!serverTag.contains(ROOT_KEY)) return profile;

        CompoundTag tag = NbtProfileAccess.child(serverTag, ROOT_KEY);
        try {
            profile.kind = LoginKind.valueOf(NbtProfileAccess.string(tag, "kind", "official")
                .toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            profile.kind = LoginKind.OFFICIAL;
        }
        profile.offlineName = NbtProfileAccess.string(tag, "offlineName", "");
        profile.yggdrasilRoot = NbtProfileAccess.string(tag, "yggdrasilRoot", "");
        profile.account = NbtProfileAccess.string(tag, "account", "");
        profile.playerName = NbtProfileAccess.string(tag, "playerName", "");
        profile.playerId = NbtProfileAccess.string(tag, "playerId", "");
        profile.accessToken = NbtProfileAccess.string(tag, "accessToken", "");
        profile.clientToken = NbtProfileAccess.string(tag, "clientToken", "");
        profile.storePassword = NbtProfileAccess.bool(tag, "storePassword", false);
        profile.password = profile.storePassword
            ? NbtProfileAccess.string(tag, "password", "") : "";
        return profile;
    }

    public static void write(CompoundTag serverTag, LoginProfile source)
    {
        LoginProfile profile = source == null ? new LoginProfile() : source;
        if (profile.isDefault()) {
            serverTag.remove(ROOT_KEY);
            return;
        }

        CompoundTag tag = new CompoundTag();
        tag.putInt("version", FORMAT_VERSION);
        tag.putString("kind", profile.kind.name().toLowerCase(Locale.ROOT));
        put(tag, "offlineName", profile.offlineName);
        put(tag, "yggdrasilRoot", profile.yggdrasilRoot);
        put(tag, "account", profile.account);
        put(tag, "playerName", profile.playerName);
        put(tag, "playerId", profile.playerId);
        put(tag, "accessToken", profile.accessToken);
        put(tag, "clientToken", profile.clientToken);
        tag.putBoolean("storePassword", profile.storePassword);
        if (profile.storePassword) put(tag, "password", profile.password);
        serverTag.put(ROOT_KEY, tag);
    }

    private static void put(CompoundTag tag, String key, String value)
    {
        if (value != null && !value.isEmpty()) tag.putString(key, value);
    }
}
