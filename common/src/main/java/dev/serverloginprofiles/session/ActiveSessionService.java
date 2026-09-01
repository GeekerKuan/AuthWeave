package dev.serverloginprofiles.session;

import com.mojang.authlib.minecraft.MinecraftSessionService;

/** Volatile pointer used by asynchronous skin-cache work after an identity switch. */
public final class ActiveSessionService
{
    private static volatile MinecraftSessionService current;

    private ActiveSessionService() {}

    public static MinecraftSessionService currentOr(MinecraftSessionService fallback)
    {
        MinecraftSessionService selected = current;
        return selected == null ? fallback : selected;
    }

    public static void select(MinecraftSessionService service)
    {
        current = service;
    }
}
