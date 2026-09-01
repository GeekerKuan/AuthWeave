package dev.serverloginprofiles.yggdrasil;

import java.util.UUID;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

/** Authlib 6 mutable-profile adapter. */
final class AuthlibProfileAdapter
{
    private AuthlibProfileAdapter() {}

    static Property packedTextures(GameProfile profile)
    {
        return profile.getProperties().get("textures").stream().findFirst().orElse(null);
    }

    static GameProfile create(UUID id, String name, Multimap<String, Property> properties)
    {
        GameProfile profile = new GameProfile(id, name);
        profile.getProperties().putAll(properties);
        return profile;
    }
}
