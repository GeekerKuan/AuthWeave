package dev.serverloginprofiles.yggdrasil;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.yggdrasil.ProfileResult;

import dev.serverloginprofiles.profile.LoginProfile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Protocol coverage using an entirely local Yggdrasil fixture. */
public class ThirdPartySessionServiceTest
{
    private static final String ID = "0123456789abcdef0123456789abcdef";
    private HttpServer fixture;
    private String root;
    private final AtomicReference<JsonObject> joinPayload = new AtomicReference<>();

    /** Starts the local fixture. */
    @BeforeEach
    public void start() throws Exception
    {
        fixture = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        root = "http://127.0.0.1:" + fixture.getAddress().getPort() + "/yggdrasil";
        fixture.createContext("/yggdrasil/authserver/authenticate", exchange -> respond(exchange, 200,
            "{\"accessToken\":\"test-access\",\"clientToken\":\"test-client\","
                + "\"selectedProfile\":{\"id\":\"" + ID + "\",\"name\":\"FixturePlayer\"}}"));
        fixture.createContext("/yggdrasil/sessionserver/session/minecraft/join", exchange -> {
            joinPayload.set(readObject(exchange));
            respond(exchange, 204, "");
        });
        fixture.createContext("/yggdrasil/sessionserver/session/minecraft/profile/" + ID, exchange -> {
            String textureJson = "{\"textures\":{"
                + "\"SKIN\":{\"url\":\"https://textures.invalid/skin.png\","
                + "\"metadata\":{\"model\":\"slim\"}},"
                + "\"CAPE\":{\"url\":\"https://textures.invalid/cape.png\"},"
                + "\"ELYTRA\":{\"url\":\"https://textures.invalid/elytra.png\"}}}";
            String packed = Base64.getEncoder().encodeToString(textureJson.getBytes(StandardCharsets.UTF_8));
            respond(exchange, 200, "{\"id\":\"" + ID + "\",\"name\":\"FixturePlayer\","
                + "\"properties\":[{\"name\":\"textures\",\"value\":\"" + packed + "\"}]}");
        });
        fixture.start();
    }

    /** Stops the local fixture. */
    @AfterEach
    public void stop()
    {
        fixture.stop(0);
    }

    /** Covers authentication, join, profile lookup, and all three texture types. */
    @Test
    public void completesTheSupportedProtocol() throws Exception
    {
        LoginProfile login = YggdrasilLoginClient.login(root, "fixture@example.invalid", "fixture-password");
        assertEquals("FixturePlayer", login.playerName);
        assertEquals("test-access", login.accessToken);

        ThirdPartySessionService sessions = new ThirdPartySessionService(root);
        UUID id = UUID.fromString("01234567-89ab-cdef-0123-456789abcdef");
        sessions.joinServer(id, login.accessToken, "server-hash");
        assertEquals("server-hash", joinPayload.get().get("serverId").getAsString());

        ProfileResult result = sessions.fetchProfile(id, true);
        assertNotNull(result);
        MinecraftProfileTextures textures = sessions.getTextures(result.profile());
        assertEquals("https://textures.invalid/skin.png", textures.skin().getUrl());
        assertEquals("slim", textures.skin().getMetadata("model"));
        assertEquals("https://textures.invalid/cape.png", textures.cape().getUrl());
        assertEquals("https://textures.invalid/elytra.png", textures.elytra().getUrl());
    }

    private static JsonObject readObject(HttpExchange exchange) throws java.io.IOException
    {
        String text = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return JsonParser.parseString(text).getAsJsonObject();
    }

    private static void respond(HttpExchange exchange, int status, String text) throws java.io.IOException
    {
        byte[] body = text.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, body.length == 0 ? -1 : body.length);
        if (body.length != 0) exchange.getResponseBody().write(body);
        exchange.close();
    }
}
