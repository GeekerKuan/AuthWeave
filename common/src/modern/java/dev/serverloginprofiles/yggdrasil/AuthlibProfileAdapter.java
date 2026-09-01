package dev.serverloginprofiles.yggdrasil;

import java.util.UUID;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

/** Authlib 7+ record adapter. */
final class AuthlibProfileAdapter
{
    private AuthlibProfileAdapter() {}

    static Property packedTextures(GameProfile profile)
    {
        return profile.properties().get("textures").stream().findFirst().orElse(null);
    }

    static GameProfile create(UUID id, String name, Multimap<String, Property> properties)
    {
        return new GameProfile(id, name, new PropertyMap(properties));
    }
}
