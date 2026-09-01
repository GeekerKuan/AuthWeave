package dev.serverloginprofiles.yggdrasil;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.serverloginprofiles.profile.LoginKind;
import dev.serverloginprofiles.profile.LoginProfile;

/** Performs the explicit password-to-token exchange for a custom provider. */
public final class YggdrasilLoginClient
{
    private YggdrasilLoginClient() {}

    /** Authenticates and returns a new token-bearing profile. */
    public static LoginProfile login(String endpoint, String account, String password)
        throws IOException, InterruptedException
    {
        final String root;
        try {
            root = YggdrasilEndpoint.normalize(endpoint);
        } catch (IllegalArgumentException invalidEndpoint) {
            throw new YggdrasilLoginException("serverloginprofiles.error.endpoint");
        }
        String clientToken = UUID.randomUUID().toString();
        JsonObject payload = new JsonObject();
        JsonObject agent = new JsonObject();
        agent.addProperty("name", "Minecraft");
        agent.addProperty("version", 1);
        payload.add("agent", agent);
        payload.addProperty("username", account);
        payload.addProperty("password", password);
        payload.addProperty("clientToken", clientToken);
        payload.addProperty("requestUser", true);

        HttpRequest request = HttpRequest.newBuilder(URI.create(root + "/authserver/authenticate"))
            .timeout(Duration.ofSeconds(20))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
            .build();
        HttpResponse<String> response = YggdrasilHttp.CLIENT.send(
            request, HttpResponse.BodyHandlers.ofString()
        );
        if (response.statusCode() / 100 != 2) throw readError(response);

        final JsonObject reply;
        try {
            reply = JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (RuntimeException malformedResponse) {
            throw new YggdrasilLoginException("serverloginprofiles.error.invalid_response");
        }
        JsonObject selected = reply.has("selectedProfile") ? reply.getAsJsonObject("selectedProfile") : null;
        if (selected == null) {
            JsonArray choices = reply.getAsJsonArray("availableProfiles");
            if (choices != null && !choices.isEmpty()) selected = choices.get(0).getAsJsonObject();
        }
        if (selected == null) {
            throw new YggdrasilLoginException("serverloginprofiles.error.no_profile");
        }

        try {
            LoginProfile result = new LoginProfile();
            result.kind = LoginKind.YGGDRASIL;
            result.yggdrasilRoot = root;
            result.account = account;
            result.playerName = selected.get("name").getAsString();
            result.playerId = selected.get("id").getAsString();
            result.accessToken = reply.get("accessToken").getAsString();
            result.clientToken = reply.has("clientToken")
                ? reply.get("clientToken").getAsString() : clientToken;
            return result;
        } catch (RuntimeException incompleteResponse) {
            throw new YggdrasilLoginException("serverloginprofiles.error.invalid_response");
        }
    }

    private static YggdrasilLoginException readError(HttpResponse<String> response)
    {
        try {
            JsonObject object = JsonParser.parseString(response.body()).getAsJsonObject();
            String message = object.has("errorMessage")
                ? object.get("errorMessage").getAsString()
                : object.has("error") ? object.get("error").getAsString() : "";
            if (!message.isBlank()) {
                String limited = message.length() <= 256 ? message : message.substring(0, 256);
                return new YggdrasilLoginException(
                    "serverloginprofiles.error.provider_rejected", limited
                );
            }
        } catch (RuntimeException ignored) {
            // Never echo an arbitrary response body into logs or UI.
        }
        return new YggdrasilLoginException(
            "serverloginprofiles.error.http_status", response.statusCode()
        );
    }
}
