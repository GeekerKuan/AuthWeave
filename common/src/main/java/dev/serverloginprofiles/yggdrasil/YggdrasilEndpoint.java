package dev.serverloginprofiles.yggdrasil;

import java.net.URI;

/** Validation and URL composition for custom Yggdrasil roots. */
public final class YggdrasilEndpoint
{
    private YggdrasilEndpoint() {}

    /** Returns a normalized HTTP(S) API root. */
    public static String normalize(String input)
    {
        String candidate = input.strip();
        if (!candidate.startsWith("https://") && !candidate.startsWith("http://")) {
            candidate = "https://" + candidate;
        }
        while (candidate.endsWith("/")) candidate = candidate.substring(0, candidate.length() - 1);
        URI parsed = URI.create(candidate);
        if (parsed.getHost() == null) throw new IllegalArgumentException("Yggdrasil API root has no host");
        return candidate;
    }
}
