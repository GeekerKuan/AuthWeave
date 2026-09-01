package dev.serverloginprofiles.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.Minecraft;

import dev.serverloginprofiles.ServerLoginProfiles;

/** Stores only the harmless warning acknowledgement; credentials never enter this file. */
public final class WarningPreference
{
    private static Boolean suppressed;

    private WarningPreference() {}

    public static synchronized boolean isSuppressed()
    {
        if (suppressed != null) return suppressed;
        Path path = path();
        if (Files.notExists(path)) return suppressed = false;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8))
                .getAsJsonObject();
            return suppressed = root.has("suppressCredentialExportWarning")
                && root.get("suppressCredentialExportWarning").getAsBoolean();
        } catch (IOException | RuntimeException error) {
            ServerLoginProfiles.LOG.warn("Unable to read warning preference from {}", path, error);
            return suppressed = false;
        }
    }

    public static synchronized void suppressFutureWarnings()
    {
        suppressed = true;
        Path destination = path();
        Path staging = destination.resolveSibling(destination.getFileName() + ".new");
        JsonObject root = new JsonObject();
        root.addProperty("suppressCredentialExportWarning", true);
        try {
            Files.createDirectories(destination.getParent());
            Files.writeString(staging, root.toString(), StandardCharsets.UTF_8);
            try {
                Files.move(staging, destination, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException unsupportedAtomicMove) {
                Files.move(staging, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException error) {
            ServerLoginProfiles.LOG.warn("Unable to write warning preference to {}", destination, error);
        }
    }

    private static Path path()
    {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config")
            .resolve("server-login-profiles-client.json");
    }
}
