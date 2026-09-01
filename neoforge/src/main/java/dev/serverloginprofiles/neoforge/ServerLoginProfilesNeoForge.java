package dev.serverloginprofiles.neoforge;

import net.neoforged.fml.common.Mod;

import dev.serverloginprofiles.ServerLoginProfiles;

/** NeoForge entry point. */
@Mod(ServerLoginProfiles.ID)
public final class ServerLoginProfilesNeoForge
{
    /** Initializes the client-only mod. */
    public ServerLoginProfilesNeoForge()
    {
        ServerLoginProfiles.initialize();
    }
}
