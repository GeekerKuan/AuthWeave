package dev.serverloginprofiles.fabric;

import net.fabricmc.api.ClientModInitializer;

import dev.serverloginprofiles.ServerLoginProfiles;

/** Fabric entry point. */
public final class ServerLoginProfilesFabric implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        ServerLoginProfiles.initialize();
    }
}
