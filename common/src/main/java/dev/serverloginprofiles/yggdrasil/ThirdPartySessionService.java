package dev.serverloginprofiles.yggdrasil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.SignatureState;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.ProfileResult;

/** Minecraft session protocol implementation for the selected third-party provider. */
public final class ThirdPartySessionService implements MinecraftSessionService
{
    private final String sessionBase;

    /** Creates a service from a provider's advertised API root. */
    public ThirdPartySessionService(String apiRoot)
    {
        sessionBase = YggdrasilEndpoint.normalize(apiRoot) + "/sessionserver/session/minecraft";
    }

    @Override
    public void joinServer(UUID profileId, String accessToken, String serverId) throws AuthenticationException
    {
        JsonObject data = new JsonObject();
        data.addProperty("accessToken", accessToken);
        data.addProperty("selectedProfile", compactUuid(profileId));
        data.addProperty("serverId", serverId);
        HttpRequest request = HttpRequest.newBuilder(URI.create(sessionBase + "/join"))
            .timeout(Duration.ofSeconds(15))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(data.toString()))
            .build();
        try {
            HttpResponse<String> response = YggdrasilHttp.CLIENT.send(
                request, HttpResponse.BodyHandlers.ofString()
            );
            if (response.statusCode() / 100 != 2) {
                throw new AuthenticationException("Third-party join rejected with HTTP " + response.statusCode());
            }
        } catch (IOException error) {
            throw new AuthenticationException("Third-party session server is unavailable", error);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new AuthenticationException("Third-party join was interrupted", error);
        }
    }

    @Override
    public ProfileResult hasJoinedServer(String username, String serverId, InetAddress address)
        throws AuthenticationUnavailableException
    {
        StringBuilder target = new StringBuilder(sessionBase).append("/hasJoined?username=")
            .append(encode(username)).append("&serverId=").append(encode(serverId));
        if (address != null) target.append("&ip=").append(encode(address.getHostAddress()));
        try {
            HttpResponse<String> response = get(target.toString());
            if (response.statusCode() == 204 || response.statusCode() == 404) return null;
            if (response.statusCode() / 100 != 2) {
                throw new AuthenticationUnavailableException("Third-party session check failed");
            }
            return decodeProfile(JsonParser.parseString(response.body()).getAsJsonObject());
        } catch (IOException error) {
            throw new AuthenticationUnavailableException(error);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new AuthenticationUnavailableException(error);
        }
    }

    @Override
    public Property getPackedTextures(GameProfile profile)
    {
        return AuthlibProfileAdapter.packedTextures(profile);
    }

    @Override
    public MinecraftProfileTextures unpackTextures(Property packed)
    {
        if (packed == null) return MinecraftProfileTextures.EMPTY;
        try {
            byte[] bytes = Base64.getDecoder().decode(packed.value());
            JsonObject root = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject textures = root.getAsJsonObject("textures");
            if (textures == null) return MinecraftProfileTextures.EMPTY;
            SignatureState signature = packed.hasSignature() ? SignatureState.SIGNED : SignatureState.UNSIGNED;
            return new MinecraftProfileTextures(
                texture(textures, "SKIN"), texture(textures, "CAPE"), texture(textures, "ELYTRA"), signature
            );
        } catch (RuntimeException malformedPayload) {
            return MinecraftProfileTextures.EMPTY;
        }
    }

    @Override
    public ProfileResult fetchProfile(UUID profileId, boolean requireSecure)
    {
        try {
            HttpResponse<String> response = get(sessionBase + "/profile/" + compactUuid(profileId)
                + "?unsigned=false");
            if (response.statusCode() / 100 != 2) return null;
            return decodeProfile(JsonParser.parseString(response.body()).getAsJsonObject());
        } catch (IOException error) {
            return null;
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public String getSecurePropertyValue(Property property) throws InsecurePublicKeyException
    {
        // The user explicitly selected this provider, so its profile-property key is the trust boundary.
        return property.value();
    }

    private static ProfileResult decodeProfile(JsonObject source)
    {
        UUID id = expandUuid(source.get("id").getAsString());
        Multimap<String, Property> values = ArrayListMultimap.create();
        if (source.has("properties")) {
            for (JsonElement entry : source.getAsJsonArray("properties")) {
                JsonObject object = entry.getAsJsonObject();
                String name = object.get("name").getAsString();
                String value = object.get("value").getAsString();
                values.put(name, object.has("signature")
                    ? new Property(name, value, object.get("signature").getAsString())
                    : new Property(name, value));
            }
        }
        return new ProfileResult(AuthlibProfileAdapter.create(
            id, source.get("name").getAsString(), values
        ));
    }

    private static MinecraftProfileTexture texture(JsonObject source, String key)
    {
        JsonObject object = source.getAsJsonObject(key);
        if (object == null || !object.has("url")) return null;
        Map<String, String> metadata = new LinkedHashMap<>();
        JsonObject rawMetadata = object.getAsJsonObject("metadata");
        if (rawMetadata != null) {
            rawMetadata.entrySet().forEach(entry -> metadata.put(entry.getKey(), entry.getValue().getAsString()));
        }
        return new MinecraftProfileTexture(object.get("url").getAsString(), metadata);
    }

    private static HttpResponse<String> get(String target) throws IOException, InterruptedException
    {
        HttpRequest request = HttpRequest.newBuilder(URI.create(target)).timeout(Duration.ofSeconds(15)).GET().build();
        return YggdrasilHttp.CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String compactUuid(UUID id)
    {
        return id.toString().replace("-", "");
    }

    private static UUID expandUuid(String value)
    {
        String text = value.replace("-", "");
        if (text.length() != 32) throw new IllegalArgumentException("Invalid profile UUID");
        return UUID.fromString(text.substring(0, 8) + "-" + text.substring(8, 12) + "-"
            + text.substring(12, 16) + "-" + text.substring(16, 20) + "-" + text.substring(20));
    }

    private static String encode(String value)
    {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
